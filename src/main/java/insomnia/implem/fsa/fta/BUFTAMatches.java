package insomnia.implem.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.TreeOp;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;

class BUFTAMatches<VAL, LBL>
{
	private IBUFTA<VAL, LBL> automaton;
	private IGFPA<VAL, LBL>  gfpa;
	private ITree<VAL, LBL>  element;

	BUFTAMatches(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		this.automaton = automaton;
		this.gfpa      = automaton.getGFPA();
		this.element   = element;
	}

	public Collection<IFSAState<VAL, LBL>> nextValidStates()
	{
		Collection<IFSAState<VAL, LBL>> states = nextValidStates_sub();

		if (!element.getRoot().isRooted())
			states = CollectionUtils.selectRejected(states, gfpa::isRooted);

		return states;
	}

	/**
	 * Process the leaves of the element.
	 * 
	 * @param nodeStatesMap map a node with all its valid states
	 * @return the remaining nodes to process in the order of processing
	 */
	private Iterator<INode<VAL, LBL>> processLeaves(Map<INode<VAL, LBL>, Collection<IFSAState<VAL, LBL>>> nodeStatesMap)
	{
		ListIterator<INode<VAL, LBL>> bottomUpNodes = TreeOp.bottomUpOrder(element).listIterator();

		while (bottomUpNodes.hasNext())
		{
			INode<VAL, LBL> node = bottomUpNodes.next();

			if (0 < element.getChildren(node).size())
			{
				bottomUpNodes.previous();
				break;
			}
			Stream<IFSAState<VAL, LBL>> stream = gfpa.getInitialStates().stream();

			if (!node.isTerminal())
				stream = stream.filter(state -> !gfpa.isTerminal(state));

			VAL value = node.getValue().orElse(null);
			stream = stream.filter(state -> state.getValueCondition().test(value));

			List<IFSAState<VAL, LBL>> states = stream.collect(Collectors.toList());

			if (states.isEmpty())
				return Collections.<INode<VAL, LBL>>emptyList().iterator();

			nodeStatesMap.put(node, states);
		}
		return bottomUpNodes;
	}

	private Collection<IFSAState<VAL, LBL>> nextValidStates_sub()
	{
		Map<INode<VAL, LBL>, Collection<IFSAState<VAL, LBL>>> nodeStatesMap   = new HashMap<>();
		List<Collection<IFSAState<VAL, LBL>>>                 childsSubStates = new ArrayList<>();

		Iterator<INode<VAL, LBL>> nodes = processLeaves(nodeStatesMap);

		while (nodes.hasNext())
		{
			INode<VAL, LBL>       node       = nodes.next();
			List<IEdge<VAL, LBL>> edgeChilds = element.getChildren(node);

			// Edge check
			for (IEdge<VAL, LBL> childEdge : edgeChilds)
			{
				INode<VAL, LBL>                 childNode   = childEdge.getChild();
				Collection<IFSAState<VAL, LBL>> childStates = nodeStatesMap.get(childNode);
				Collection<IFSAState<VAL, LBL>> newStates   = GFPAOp.getNextValidStates(gfpa, childStates, childEdge.getLabel());

				childsSubStates.add(CollectionUtils.union(IBUFTA.getInitials(automaton, childNode), newStates));
				// No need of that node anymore
				nodeStatesMap.remove(childNode);
			}
			// If one child node: nothing more to do by construction because no hyper edge exists.
			if (edgeChilds.size() <= 1)
			{
				Collection<IFSAState<VAL, LBL>> newStates = childsSubStates.get(0);

				for (IFSAState<VAL, LBL> newState : newStates)
				{
					if (gfpa.isFinal(newState))
					{
						if (!gfpa.isRooted(newState))
							return Collections.singleton(newState);
					}
				}
				nodeStatesMap.put(node, newStates);
			}
			else
			{
				// Check hyper transitions
				Collection<IFTAEdge<VAL, LBL>>  hEdges    = automaton.getHyperEdges(childsSubStates);
				Collection<IFSAState<VAL, LBL>> newStates = new HashSet<>(IBUFTA.getInitials(automaton, node));

				for (IFTAEdge<VAL, LBL> hEdge : hEdges)
				{
					if (hEdge.getCondition().testND(childsSubStates))
					{
						IFSAState<VAL, LBL> newState = hEdge.getChild();

						if (gfpa.isFinal(newState))
						{
							if (gfpa.isRooted(newState) && node != element.getRoot())
								continue;
							if (!gfpa.isRooted(newState))
								return Collections.singleton(newState);
						}
						newStates.add(newState);
					}
				}
				nodeStatesMap.put(node, newStates);
			}
			childsSubStates.clear();
		}
		return nodeStatesMap.get(element.getRoot());
	}
}