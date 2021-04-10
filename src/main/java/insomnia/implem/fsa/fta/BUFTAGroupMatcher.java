package insomnia.implem.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher.ITreeBothResults;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.data.Trees;
import insomnia.implem.data.regex.TreeMatchResults;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.lib.help.HelpLists;

class BUFTAGroupMatcher<VAL, LBL>
{
	// =========================================================================

	/**
	 * Represents the informations to be associated with a node, that is its states and the origins (leaves).
	 * 
	 * @author zuri
	 */
	private class NodeData
	{
		private Map<IFSAState<VAL, LBL>, List<From>> statesMap = new HashMap<>();

		public NodeData()
		{
		}

		/**
		 * Add an origin to a state.
		 * If the state is a initial looped one, than its previous origins are forgot.
		 */
		public void add(IFSAState<VAL, LBL> state, From from)
		{
			List<From> froms = statesMap.computeIfAbsent(state, k -> new ArrayList<>());

			if (initialLooped.contains(state))
				froms.clear();

			froms.add(from);
		}

		/**
		 * Merge an origin to a state origins.
		 * 
		 * @param state
		 * @param from
		 */
		public void merge(IFSAState<VAL, LBL> state, From from)
		{
			var presents = CollectionUtils.select(getFroms(state), from::equals);

			if (presents.isEmpty())
				add(state, from);
			else
			{
				for (From f : presents)
					f.merge(from);
			}
		}

		public Collection<IFSAState<VAL, LBL>> getStates()
		{
			return statesMap.keySet();
		}

		public List<From> getFroms(IFSAState<VAL, LBL> state)
		{
			var froms = statesMap.get(state);

			if (null == froms)
				return null;

			return froms;
		}

		@Override
		public String toString()
		{
			return statesMap.toString();
		}
	}

	/**
	 * Create the NodeData to be associated with some initial states.
	 * 
	 * @implNote
	 *           The algorithm assume that states are ordered in a way that all parents states appears before the child.
	 * @param stateClass  the states to process.
	 * @param initialLeaf the actual leaf of the element
	 * @return
	 */
	public NodeData createInitialNodeData(Collection<IFSAState<VAL, LBL>> stateClass, INode<VAL, LBL> initialLeaf)
	{
		NodeData        ret          = new NodeData();
		INode<VAL, LBL> originalNode = null;

		for (IFSAState<VAL, LBL> state : stateClass)
		{
			INode<VAL, LBL> nonode = automaton.getOriginalNodes().get(state);

			if (null != nonode)
				originalNode = nonode;

			ret.add(state, new From(initialLeaf, originalNode));
		}
		return ret;
	}

	// =========================================================================

	/**
	 * Represents an origin of a state, that is the tree leaves and a root when complete.
	 */
	private class From
	{
		private INode<VAL, LBL> elementRoot, originalRoot;

		private Map<INode<VAL, LBL>, INode<VAL, LBL>> nodeToOriginal;

		From()
		{
			nodeToOriginal = new HashMap<>();
		}

		From(INode<VAL, LBL> elementLeaf, INode<VAL, LBL> originalLeaf)
		{
			nodeToOriginal = new HashMap<>();
			elementRoot    = elementLeaf;
			originalRoot   = originalLeaf;
			nodeToOriginal.put(elementLeaf, originalLeaf);
		}

		From cpy()
		{
			From ret = new From();
			ret.nodeToOriginal.putAll(this.nodeToOriginal);
			ret.elementRoot  = elementRoot;
			ret.originalRoot = originalRoot;
			return ret;
		}

		void setRoot(INode<VAL, LBL> elementRoot, INode<VAL, LBL> originalRoot)
		{
			this.elementRoot  = elementRoot;
			this.originalRoot = originalRoot;
		}

		public void merge(From from)
		{
			elementRoot = from.elementRoot;
			if (null != from.originalRoot)
			{
				originalRoot = from.originalRoot;
				nodeToOriginal.replace(elementRoot, originalRoot);
			}
			for (var kv : from.nodeToOriginal.entrySet())
			{
				if (!nodeToOriginal.containsKey(kv.getKey()))
					nodeToOriginal.put(kv.getKey(), kv.getValue());
				else if (null != kv.getValue())
					nodeToOriginal.put(kv.getKey(), kv.getValue());
			}
		}

		public Collection<INode<VAL, LBL>> getElementLeaves()
		{
			return nodeToOriginal.keySet();
		}

		public Collection<INode<VAL, LBL>> getOriginalLeaves()
		{
			return nodeToOriginal.values();
		}

		@Override
		public int hashCode()
		{
			return 0;
		}

		@Override
		public boolean equals(Object obj)
		{
			@SuppressWarnings("unchecked")
			From from = (From) obj;
			return elementRoot == from.elementRoot //
				&& Objects.equals(getElementLeaves(), from.getElementLeaves());
		}

		@Override
		public String toString()
		{
			return new StringBuilder("from:").append(Trees.subTree(element, elementRoot, getElementLeaves())).toString();
		}
	}

	// =========================================================================

	private IBUFTA<VAL, LBL>         automaton;
	private IGFPA<VAL, LBL>          gfpa;
	private ITree<VAL, LBL>          element;
	private Queue<From>              currentResults;
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
	public ITreeBothResults<VAL, LBL> nextMatch()
	{
		if (end)
			return TreeMatchResults.emptyBoth();

		if (null == bottomUpNodes)
			bottomUpNodes = processLeaves();

		while (!end && currentResults.isEmpty())
			nextValidStep();

		if (currentResults.isEmpty())
			return TreeMatchResults.emptyBoth();

		var res = currentResults.poll();

		return TreeMatchResults.createBoth( //
			TreeMatchResults.create(element, res.elementRoot, res.getElementLeaves()), //
			TreeMatchResults.create(automaton.getOriginalTree(), res.originalRoot, res.getOriginalLeaves()) //
		);
	}

	/**
	 * Process the leaves of the element.
	 * 
	 * @param nodeStatesMap map a node with all its valid states
	 * @return the remaining nodes to process in the order of processing
	 */
	private Iterator<INode<VAL, LBL>> processLeaves()
	{
		return BUFTAMatches.processLeaves(gfpa, element, (node, stateClass) -> {
			NodeData nodeData    = nodeStatesMap.getOrDefault(node, new NodeData());
			NodeData newNodeData = createInitialNodeData(stateClass, node);

			nodeData.statesMap.putAll(newNodeData.statesMap);
			nodeStatesMap.putIfAbsent(node, nodeData);
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
			for (From from : nodeData.getFroms(state))
				currentResults.add(from);
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
			List<NodeData> childsSubStates = new ArrayList<>();

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
						INode<VAL, LBL>     originalNode      = null;

						for (var s : IGFPA.getValidStates(gfpa, newState, IGFPA.stateOnNodePredicate(gfpa, element, node)))
						{
							/*
							 * Get the original node overriding the last one.
							 * This part assume a good order parent->child in the sequence of 's'.
							 */
							{
								INode<VAL, LBL> oNode = automaton.getOriginalNodes().get(s);
								if (null != oNode)
									originalNode = oNode;
							}
							// Generate a new origin
							if (newStateIsInitial)
								newNodeData.add(s, new From(node, originalNode));
							// Update the root origin
							if (!isAnyLoop)
							{
								for (From from : childNodeData.getFroms(childState))
								{
									From newFrom = from.cpy();
									newFrom.setRoot(node, originalNode);
									newNodeData.add(s, newFrom);
								}
							}
						}
					}
				}
				childsSubStates.add(newNodeData);
			}
			NodeData nodeData = new NodeData();
			/*
			 * ================
			 * THE NODE PROCESS
			 * ================
			 * .
			 * Use hyper-transitions to compute valid node states.
			 */
			List<Collection<IFSAState<VAL, LBL>>> childsStates = CollectionUtils.collect(childsSubStates, s -> s.getStates(), new ArrayList<>());

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
					var childNodeData = childsSubStates.get(0);

					for (List<IFSAState<VAL, LBL>> validStates : validChildsStates)
					{
						var validState = validStates.get(0);

						for (From from : childNodeData.getFroms(validState))
							for (var nstate : newStates)
								nodeData.merge(nstate, from);
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

							var childNodeData = childsSubStates.get(i);
							childFroms.add(childNodeData.getFroms(validState));
						}

						for (List<From> froms : (Iterable<List<From>>) () -> HelpLists.cartesianProduct(childFroms))
						{
							From newFrom = new From();

							for (From from : froms)
								newFrom.merge(from);

							for (var nstate : newStates)
								nodeData.merge(nstate, newFrom);
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
