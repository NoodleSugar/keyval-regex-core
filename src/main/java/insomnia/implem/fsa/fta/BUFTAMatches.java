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

	private IFSAState<VAL, LBL> internalCheckFinals(Collection<IFSAState<VAL, LBL>> states)
	{
		return IterableUtils.find(states, s -> gfpa.isFinal(s) && !gfpa.isRooted(s));
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

			// Edge check
			for (IEdge<VAL, LBL> childEdge : edgeChilds)
			{
				LBL             label     = childEdge.getLabel();
				INode<VAL, LBL> childNode = childEdge.getChild();

				Collection<IFSAState<VAL, LBL>> childStates = nodeStatesMap.get(childNode);
				Collection<IFSAState<VAL, LBL>> newStates   = new HashSet<>();

				for (IFSAEdge<VAL, LBL> edge : gfpa.getEdgesOf(childStates))
				{
					if (!IGFPA.testLabel(edge.getLabelCondition(), label))
						continue;

					IFSAState<VAL, LBL> newState = edge.getChild();
					newStates.addAll(IGFPA.getValidStates(gfpa, newState, IBUFTA.statePredicate(gfpa, node)));
				}
				childsSubStates.add(newStates);

				// No need of that node anymore
				nodeStatesMap.remove(childNode);
			}
			Collection<IFSAState<VAL, LBL>> newStates = new HashSet<>();

			// Check hyper transitions
			for (IFTAEdge<VAL, LBL> hEdge : automaton.getHyperEdges(childsSubStates))
			{
				if (!hEdge.getCondition().testND(childsSubStates))
					continue;

				IFSAState<VAL, LBL> newState = hEdge.getChild();
				newStates.addAll(IGFPA.getValidStates(gfpa, newState, IBUFTA.statePredicate(gfpa, node)));
			}
			nodeStatesMap.put(node, newStates);
			childsSubStates.clear();

			// Is there a final state in the reached new states ?
			IFSAState<VAL, LBL> oneFinal = internalCheckFinals(newStates);

			if (null != oneFinal)
				return Collections.singleton(oneFinal);
		}
		return nodeStatesMap.get(element.getRoot());
	}
}