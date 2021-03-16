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
import java.util.function.BiConsumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSAEdge;
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

	// ==========================================================================

	public Collection<IFSAState<VAL, LBL>> nextValidStates()
	{
		Collection<IFSAState<VAL, LBL>> states = nextValidStates_sub();

		if (!element.getRoot().isRooted())
			states = CollectionUtils.selectRejected(states, gfpa::isRooted);

		return states;
	}

	// ==========================================================================

	/**
	 * Get the final states from states that must persist as next states.
	 * 
	 * @param states current valid states
	 */
	Collection<IFSAState<VAL, LBL>> getPersistantFinal(Collection<IFSAState<VAL, LBL>> states)
	{
		return CollectionUtils.select(states, s -> gfpa.isFinal(s) && !gfpa.isRooted(s));
	}

	private IFSAState<VAL, LBL> checkFinals(Collection<IFSAState<VAL, LBL>> states)
	{
		return IterableUtils.find(states, gfpa::isFinal);
	}

	/**
	 * Process the leaves of the element.
	 * 
	 * @return the remaining nodes to process in the order of processing
	 */
	static <VAL, LBL> Iterator<INode<VAL, LBL>> processLeaves(IGFPA<VAL, LBL> gfpa, ITree<VAL, LBL> element, BiConsumer<INode<VAL, LBL>, Collection<IFSAState<VAL, LBL>>> consume)
	{
		ListIterator<INode<VAL, LBL>> bottomUpNodes = ITree.bottomUpOrder(element).listIterator();

		while (bottomUpNodes.hasNext())
		{
			INode<VAL, LBL> node = bottomUpNodes.next();

			if (0 < element.getChildren(node).size())
			{
				bottomUpNodes.previous();
				break;
			}
			consume.accept(node, IBUFTA.getInitials(gfpa, node));
		}
		return bottomUpNodes;
	}

	private Collection<IFSAState<VAL, LBL>> nextValidStates_sub()
	{
		Map<INode<VAL, LBL>, Collection<IFSAState<VAL, LBL>>> nodeStatesMap   = new HashMap<>();
		List<Collection<IFSAState<VAL, LBL>>>                 childsSubStates = new ArrayList<>();
		Iterator<INode<VAL, LBL>>                             nodes           = processLeaves(gfpa, element, nodeStatesMap::put);

		while (nodes.hasNext())
		{
			INode<VAL, LBL>       node       = nodes.next();
			List<IEdge<VAL, LBL>> edgeChilds = element.getChildren(node);
			boolean               nodeIsRoot = !nodes.hasNext();

			// Edge check
			for (IEdge<VAL, LBL> childEdge : edgeChilds)
			{
				LBL             label     = childEdge.getLabel();
				VAL             value     = node.getValue();
				INode<VAL, LBL> childNode = childEdge.getChild();

				Collection<IFSAState<VAL, LBL>> childStates = nodeStatesMap.get(childNode);
				Collection<IFSAState<VAL, LBL>> newStates   = new HashSet<>();

				for (IFSAEdge<VAL, LBL> edge : gfpa.getReachableEdges(childStates))
				{
					IFSAState<VAL, LBL> nextState = edge.getChild();

					if (!GFPAOp.testLabel(edge.getLabelCondition(), label) || !GFPAOp.testValue(nextState.getValueCondition(), value))
						continue;

					newStates.addAll(IBUFTA.internalFilterNewStates(gfpa, gfpa.getEpsilonClosure(nextState), nodeIsRoot, nodeIsRoot));
				}
				newStates.addAll(getPersistantFinal(childStates));
				childsSubStates.add(newStates);

				// No need of that node anymore
				nodeStatesMap.remove(childNode);
			}
			Collection<IFSAState<VAL, LBL>> newStates;

			// If one child node: nothing more to do by construction because no hyper edge exists.
			if (edgeChilds.size() <= 1)
				newStates = childsSubStates.get(0);
			else
			{
				newStates = new HashSet<>();

				// Each child states must belong to the node states
				for (Collection<IFSAState<VAL, LBL>> subStates : childsSubStates)
					newStates.addAll(subStates);

				// Check hyper transitions
				Collection<IFTAEdge<VAL, LBL>> hEdges = automaton.getHyperEdges(childsSubStates);

				for (IFTAEdge<VAL, LBL> hEdge : hEdges)
				{
					if (hEdge.getCondition().testND(childsSubStates))
					{
						IFSAState<VAL, LBL> newState = hEdge.getChild();

						if (IBUFTA.internalFilterNewStates(gfpa, Collections.singleton(newState), nodeIsRoot, node.isRooted()).isEmpty())
							continue;

						newStates.add(newState);
					}
				}
			}
			Collection<IFSAState<VAL, LBL>> initials = IBUFTA.internalGetInitials(automaton.getGFPA(), node);
			nodeStatesMap.put(node, CollectionUtils.union(newStates, initials));
			childsSubStates.clear();

			// Is there a final state in the reached new states ?
			IFSAState<VAL, LBL> oneFinal = checkFinals(newStates);

			if (null != oneFinal)
				return Collections.singleton(oneFinal);
		}
		return nodeStatesMap.get(element.getRoot());
	}
}