package insomnia.implem.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.creational.ISubTreeBuilder;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.data.creational.SubTreeBuilder;
import insomnia.implem.data.regex.TreeMatchResults;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.lib.help.HelpLists;

class BUFTAGroupMatcher<VAL, LBL>
{
	// =========================================================================

	/**
	 * Stores states/From informations of a node.
	 * 
	 * @author zuri
	 */
	private class NodeData
	{
		private Map<IFSAState<VAL, LBL>, List<From>> statesMap = new HashMap<>();

		public NodeData()
		{
		}

		NodeData(Collection<IFSAState<VAL, LBL>> states, From from)
		{
			for (IFSAState<VAL, LBL> state : states)
			{
				List<From> froms = new ArrayList<>();
				statesMap.put(state, froms);
				froms.add(from.cpy());
			}
		}

		public void add(IFSAState<VAL, LBL> state, From from)
		{
			List<From> froms = statesMap.computeIfAbsent(state, k -> new ArrayList<>());

			if (!initialLooped.contains(state))
				froms.add(from.cpy());
		}

		public boolean contains(IFSAState<VAL, LBL> state, From from)
		{
			Collection<From> froms = getFroms(state, null);

			if (null == froms)
				return false;

			return IterableUtils.matchesAny(froms, f -> ITree.sameAs(f.builder, from.builder));
		}

		public Collection<IFSAState<VAL, LBL>> getStates()
		{
			return statesMap.keySet();
		}

		public List<From> getFroms(IFSAState<VAL, LBL> state, INode<VAL, LBL> currentNodeForInitialCase)
		{
			var froms = statesMap.get(state);

			if (null == froms)
				return null;

			if (froms.isEmpty() && null != currentNodeForInitialCase)
				return Collections.singletonList(new From(currentNodeForInitialCase));

			return froms;

		}

		@Override
		public String toString()
		{
			return statesMap.toString();
		}
	}

	/**
	 * Store the scanned edges before a node.
	 * 
	 * @author zuri
	 */
	private class From
	{
		private ISubTreeBuilder<VAL, LBL> builder;

		From(INode<VAL, LBL> from)
		{
			this.builder = new SubTreeBuilder<>(element).setRoot(from);
		}

		From(ISubTreeBuilder<VAL, LBL> builder)
		{
			this.builder = builder;
		}

		public From cpy()
		{
			return new From(new SubTreeBuilder<>(builder).addTree(builder.getRoot()));
		}

		@Override
		public String toString()
		{
			return builder.toString();
		}
	}

	// =========================================================================

	private IBUFTA<VAL, LBL>         automaton;
	private IGFPA<VAL, LBL>          gfpa;
	private ITree<VAL, LBL>          element;
	private Queue<ITree<VAL, LBL>>   currentResults;
	private Set<IFSAState<VAL, LBL>> initialLooped;

	private boolean end = false;

	// =========================================================================

	public static <VAL, LBL> BUFTAGroupMatcher<VAL, LBL> create(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return new BUFTAGroupMatcher<VAL, LBL>(automaton, element);
	}

	// =========================================================================

	private BUFTAGroupMatcher(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		this.automaton      = automaton;
		this.gfpa           = automaton.getGFPA();
		this.element        = element;
		this.currentResults = new LinkedList<>();
		this.initialLooped  = CollectionUtils.select(gfpa.getInitialStates(), s -> IGFPA.hasAnyLoop(gfpa, s), new HashSet<>());
	}

	// =========================================================================

	private Map<INode<VAL, LBL>, NodeData> nodeStatesMap = new HashMap<>();

	private Iterator<INode<VAL, LBL>> bottomUpNodes;

	/**
	 * @return the next match if exists or {@link TreeMatchResults#empty()}
	 */
	public ITreeMatchResult<VAL, LBL> nextMatch()
	{
		if (end)
			return TreeMatchResults.empty();

		if (null == bottomUpNodes)
			bottomUpNodes = processLeaves();

		while (!end && currentResults.isEmpty())
			nextValidStep();

		if (currentResults.isEmpty())
			return TreeMatchResults.empty();

		return TreeMatchResults.create(currentResults.poll());
	}

	/**
	 * Process the leaves of the element.
	 * 
	 * @param nodeStatesMap map a node with all its valid states
	 * @return the remaining nodes to process in the order of processing
	 */
	private Iterator<INode<VAL, LBL>> processLeaves()
	{
		return BUFTAMatches.processLeaves(gfpa, element, (node, states) -> {
			NodeData nodeData = new NodeData(states, new From(node));
			nodeStatesMap.put(node, nodeData);
			checkFinals(node, nodeData);
		});
	}

	private boolean checkFinals(INode<VAL, LBL> node, NodeData nodeData)
	{
		Collection<IFSAState<VAL, LBL>> finalStates = CollectionUtils.select(nodeData.getStates(), gfpa::isFinal);

		if (finalStates.isEmpty())
			return false;

		for (IFSAState<VAL, LBL> state : finalStates)
		{
			for (From from : nodeData.getFroms(state, node))
				currentResults.add(from.builder);
		}
		return true;
	}

	private void nextValidStep()
	{
		while (bottomUpNodes.hasNext())
		{
			INode<VAL, LBL>       node       = bottomUpNodes.next();
			List<IEdge<VAL, LBL>> edgeChilds = element.getChildren(node);
			int                   nbChilds   = edgeChilds.size();

			/*
			 * =============
			 * CHILD PROCESS
			 * =============
			 * .
			 * For each child edge compute the valid states according to the this.gfpa.
			 * This part result in the variable 'childsSubStates' which contains, in order, the NodeData of a node.
			 */
			List<Pair<INode<VAL, LBL>, NodeData>> childsSubStates = new ArrayList<>();

			for (IEdge<VAL, LBL> childEdge : edgeChilds)
			{
				LBL             label     = childEdge.getLabel();
				INode<VAL, LBL> childNode = childEdge.getChild();

				NodeData childNodeData = nodeStatesMap.get(childNode);
				NodeData newNodeData   = new NodeData();

				// Scan each child state to know new states origin
				for (IFSAState<VAL, LBL> childState : childNodeData.getStates())
				{
					for (IFSAEdge<VAL, LBL> edge : gfpa.getEdgesOf(childState))
					{
						if (!IGFPA.testLabel(edge.getLabelCondition(), label))
							continue;

						IFSAState<VAL, LBL> newState          = edge.getChild();
						boolean             isAnyLoop         = FSALabelConditions.isAnyLoop(edge.getLabelCondition());
						boolean             newStateIsInitial = gfpa.isInitial(newState);
						/*
						 * For each new valid states update the From.builders
						 */
						for (var s : IGFPA.getValidStates(gfpa, newState, IGFPA.stateOnNodePredicate(gfpa, element, node)))
						{
							/*
							 * Generate a new From
							 */
							if (newStateIsInitial)
								newNodeData.add(s, new From(node));
							/*
							 * Update builders.
							 * Note: A starting looped initial state does not store any builder.
							 */
							if (!isAnyLoop)
							{
								From newFrom = new From(node);
								newFrom.builder.add(childEdge).setRoot(node);

								for (From from : childNodeData.getFroms(childState, node))
									newFrom.builder.addTree(from.builder, from.builder.getRoot());

								newNodeData.add(s, newFrom);
							}
						}
					}
				}
				childsSubStates.add(Pair.of(childNode, newNodeData));
			}
			NodeData nodeData = new NodeData();
			/*
			 * ================
			 * THE NODE PROCESS
			 * ================
			 * .
			 * Use hyper-transitions to compute valid node states.
			 */
			List<Collection<IFSAState<VAL, LBL>>> childsStates = CollectionUtils.collect(childsSubStates, s -> s.getRight().getStates(), new ArrayList<>());

			for (IFTAEdge<VAL, LBL> hEdge : automaton.getHyperEdges(childsStates))
			{
				Collection<List<IFSAState<VAL, LBL>>> validChildsStates = hEdge.getCondition().validStatesND(childsStates);

				if (validChildsStates.isEmpty())
					continue;

				IFSAState<VAL, LBL>             newState  = hEdge.getChild();
				Collection<IFSAState<VAL, LBL>> newStates = IGFPA.getValidStates(gfpa, newState, IGFPA.stateOnNodePredicate(gfpa, element, node));

				if (newStates.isEmpty())
					continue;

				// Simplest case
				if (nbChilds == 1)
				{
					var childNodeData = childsSubStates.get(0).getRight();

					for (List<IFSAState<VAL, LBL>> validStates : validChildsStates)
					{
						var validState = validStates.get(0);

						for (From from : childNodeData.getFroms(validState, node))
							for (var nstate : newStates)
								if (!nodeData.contains(nstate, from))
									nodeData.add(nstate, from);
					}
				}
				// Multiple childs
				else
				{
					// Take each valid combination and merge builders
					for (List<IFSAState<VAL, LBL>> validStates : validChildsStates)
					{
						List<Collection<From>> childFroms = new ArrayList<>();

						for (int i = 0; i < nbChilds; i++)
						{
							IFSAState<VAL, LBL> validState = validStates.get(i);

							if (null == validState)
								continue;

							var childNodeData = childsSubStates.get(i).getRight();
							childFroms.add(childNodeData.getFroms(validState, node));
						}

						for (List<From> froms : (Iterable<List<From>>) () -> HelpLists.cartesianProduct(childFroms))
						{
							From newFrom = new From(node);

							for (From from : froms)
								newFrom.builder.addTree(from.builder, from.builder.getRoot());

							for (var nstate : newStates)
								if (!nodeData.contains(nstate, newFrom))
									nodeData.add(nstate, newFrom);
						}
					}
				}
			}
			// Clean unneeded
			for (IEdge<VAL, LBL> edgeChild : edgeChilds)
				nodeStatesMap.remove(edgeChild.getChild());

			nodeStatesMap.put(node, nodeData);
			childsSubStates.clear();

			if (checkFinals(node, nodeData))
				return;
		}
		end();
	}

	private void end()
	{
		end = true;
	}
}
