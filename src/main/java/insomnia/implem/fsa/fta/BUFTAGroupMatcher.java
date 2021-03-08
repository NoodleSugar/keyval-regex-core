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
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.creational.ISubTreeBuilder;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.data.creational.SubTreeBuilder;
import insomnia.implem.data.regex.TreeMatchResults;

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

		public NodeData(Collection<IFSAState<VAL, LBL>> states)
		{
			add(states);
		}

		public void add(NodeData src)
		{
			for (IFSAState<VAL, LBL> state : src.getStates())
				statesMap.put(state, new ArrayList<>(src.getFroms(state)));
		}

		public void add(Collection<IFSAState<VAL, LBL>> states)
		{
			for (IFSAState<VAL, LBL> state : states)
				statesMap.put(state, new ArrayList<>());
		}

		public void add(IFSAState<VAL, LBL> state, From from)
		{
			add(Collections.singleton(state), from);
		}

		public void reset(Collection<IFSAState<VAL, LBL>> states)
		{
			for (IFSAState<VAL, LBL> state : states)
				statesMap.put(state, new ArrayList<>());
		}

		public void add(Collection<IFSAState<VAL, LBL>> states, From from)
		{
			for (IFSAState<VAL, LBL> state : states)
			{
				List<From> froms = statesMap.computeIfAbsent(state, k -> new ArrayList<>());
				froms.add(from.cpy());
			}
		}

		public Collection<IFSAState<VAL, LBL>> getStates()
		{
			return statesMap.keySet();
		}

		public Collection<List<From>> getFroms()
		{
			return statesMap.values();
		}

		public List<From> getFroms(IFSAState<VAL, LBL> state)
		{
			return statesMap.get(state);
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
			return new From(new SubTreeBuilder<>(builder).addTree(builder, builder.getRoot()));
		}

		@Override
		public String toString()
		{
			return builder.toString();
		}
	}

	// =========================================================================

	private IBUFTA<VAL, LBL>       automaton;
	private IGFPA<VAL, LBL>        gfpa;
	private ITree<VAL, LBL>        element;
	private Queue<ITree<VAL, LBL>> currentResults;

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

	private void addInitials(INode<VAL, LBL> node, NodeData nodeData)
	{
		Collection<IFSAState<VAL, LBL>> initialStates = IBUFTA.getInitials(automaton, node);

		if (initialStates.isEmpty())
			return;

		nodeData.add(initialStates, new From(node));
	}

	private boolean checkFinals(INode<VAL, LBL> node, NodeData nodeData)
	{
		Collection<IFSAState<VAL, LBL>> finalStates = CollectionUtils.select(nodeData.getStates(), //
			s -> gfpa.isFinal(s));
		if (finalStates.isEmpty())
			return false;

		Collection<IFSAState<VAL, LBL>> toClean = new ArrayList<>(finalStates.size());

		for (IFSAState<VAL, LBL> state : finalStates)
		{
			IGFPA<VAL, LBL> fpa = gfpa;
			if (fpa.isRooted(state) && !node.isRooted())
			{
				toClean.add(state);
				continue;
			}
			for (From from : nodeData.getFroms(state))
				currentResults.add(from.builder);

			if (!fpa.isRooted(state) || fpa.getAllEdgesOf(state).size() > 1)
				toClean.add(state);
		}
		nodeData.reset(toClean);
		return true;
	}

	private void nextValidStep()
	{
		while (bottomUpNodes.hasNext())
		{
			List<Pair<INode<VAL, LBL>, NodeData>> childsSubStates = new ArrayList<>();

			NodeData              nodeData   = null;
			INode<VAL, LBL>       node       = bottomUpNodes.next();
			List<IEdge<VAL, LBL>> edgeChilds = element.getChildren(node);

			/*
			 * Compute the states from the child edges and update the builder of each parent giving new states
			 */
			for (IEdge<VAL, LBL> childEdge : edgeChilds)
			{
				INode<VAL, LBL> childNode = childEdge.getChild();
				nodeData = nodeStatesMap.get(childNode);
				Collection<IFSAState<VAL, LBL>> childStates = nodeData.getStates();
				NodeData                        newNodeData = new NodeData(Collections.emptyList());

				// Scan each child state to know new states origin
				for (IFSAState<VAL, LBL> childState : childStates)
				{
					Collection<IFSAState<VAL, LBL>> childNextStates = GFPAOp.getNextValidStates(gfpa, Collections.singleton(childState), childEdge.getLabel(), childEdge.getChild().getValue());

					if (!childNextStates.isEmpty())
					{
						From newFrom = new From(node);
						newFrom.builder.add(childEdge).setRoot(node);
						// Update the new builder informations
						for (From from : nodeData.getFroms(childState))
							newFrom.builder.addTree(from.builder, from.builder.getRoot());

						newNodeData.add(childNextStates, newFrom);
					}
				}
				childsSubStates.add(Pair.of(childNode, newNodeData));
			}

			// If one child node: nothing more to do by construction because no hyper edge exists.
			if (edgeChilds.size() <= 1)
				nodeData = childsSubStates.get(0).getRight();
			else
			{
				nodeData = new NodeData();

				for (Pair<INode<VAL, LBL>, NodeData> css : childsSubStates)
					nodeData.add(css.getRight());

				List<Collection<IFSAState<VAL, LBL>>> childsStates = childsSubStates.stream().map(s -> s.getRight().getStates()).collect(Collectors.toList());

				Collection<IFTAEdge<VAL, LBL>>  hEdges    = automaton.getHyperEdges(childsStates);
				Collection<IFSAState<VAL, LBL>> newStates = new HashSet<>();

				for (IFTAEdge<VAL, LBL> hEdge : hEdges)
				{
					Collection<List<IFSAState<VAL, LBL>>> validChildsStates = hEdge.getCondition().validStatesND(childsStates);

					if (validChildsStates.isEmpty())
						continue;

					newStates.add(hEdge.getChild());

					// Take each valid combination and merge builders
					for (List<IFSAState<VAL, LBL>> validStates : validChildsStates)
					{
						From newFrom = new From(node);

						for (int i = 0; i < childsSubStates.size(); i++)
						{
							IFSAState<VAL, LBL> validState = validStates.get(i);

							if (null == validState)
								continue;

							NodeData edgeChildData = childsSubStates.get(i).getRight();
							for (List<From> froms : edgeChildData.getFroms())
							{
								for (From from : froms)
									newFrom.builder.addTree(from.builder, from.builder.getRoot());
							}
						}
						nodeData.add(hEdge.getChild(), newFrom);
					}
				}
			}
			// Clean unneeded
			for (IEdge<VAL, LBL> edgeChild : edgeChilds)
				nodeStatesMap.remove(edgeChild.getChild());

			nodeStatesMap.put(node, nodeData);
			addInitials(node, nodeData);
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
