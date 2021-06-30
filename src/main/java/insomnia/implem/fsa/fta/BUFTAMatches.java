package insomnia.implem.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

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

	// ==========================================================================

	private BUFTAMatches(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		this.automaton = automaton;
		this.gfpa      = automaton.getGFPA();
		this.element   = element;
	}

	public static <VAL, LBL> BUFTAMatches<VAL, LBL> create(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return new BUFTAMatches<>(automaton, element);
	}

	// ==========================================================================

	private boolean hasFinal(Collection<IFSAState<VAL, LBL>> states)
	{
		return IterableUtils.matchesAny(states, gfpa::isFinal);
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
			for (var _class : IGFPA.getInitialClasses(gfpa, element, node))
				consume.accept(node, _class);
		}
		return bottomUpNodes;
	}

	private boolean finalLeaf = false;

	public boolean matches()
	{
		MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap   = new ArrayListValuedHashMap<>();
		List<Collection<IFSAState<VAL, LBL>>>                childsSubStates = new ArrayList<>();

		finalLeaf = false;
		Iterator<INode<VAL, LBL>> nodes = processLeaves(gfpa, element, //
			(node, states) -> //
			{
				if (hasFinal(states))
				{
					finalLeaf = true;
					return;
				}
				nodeStatesMap.putAll(node, states);
			});

		if (finalLeaf)
			return true;

		while (nodes.hasNext())
		{
			INode<VAL, LBL>       node       = nodes.next();
			List<IEdge<VAL, LBL>> edgeChilds = element.getChildren(node);

			Predicate<IFSAState<VAL, LBL>> stateOnNodePredicate = IGFPA.stateOnNodePredicate(gfpa, element, node);

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
					newStates.addAll(IGFPA.getValidStates(gfpa, newState, stateOnNodePredicate));
				}
				childsSubStates.add(newStates);

				// No need of that node anymore
				nodeStatesMap.remove(childNode);
			}
			Collection<IFSAState<VAL, LBL>> newStates = new HashSet<>();

			// Check hyper transitions
			for (IFTAEdge<VAL, LBL> hEdge : automaton.getFTAEdges(childsSubStates))
			{
				if (!hEdge.getConditionFactory().apply(automaton, hEdge).testND(childsSubStates))
					continue;

				IFSAState<VAL, LBL> newState = hEdge.getChild();
				newStates.addAll(IGFPA.getValidStates(gfpa, newState, stateOnNodePredicate));
			}
			nodeStatesMap.putAll(node, newStates);
			childsSubStates.clear();

			if (hasFinal(newStates))
				return true;
		}
		return hasFinal(nodeStatesMap.get(element.getRoot()));
	}
}