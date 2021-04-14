package insomnia.implem.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

final class BUFTAPreStateMatcher<VAL, LBL>
{
	private IBUFTA<VAL, LBL> automaton;
	private IGFPA<VAL, LBL>  gfpa;
	private ITree<VAL, LBL>  element;

	// ==========================================================================

	BUFTAPreStateMatcher(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		reset(automaton, element);
	}

	public static <VAL, LBL> BUFTAPreStateMatcher<VAL, LBL> create(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return new BUFTAPreStateMatcher<>(automaton, element);
	}

	// ==========================================================================

	public BUFTAPreStateMatcher<VAL, LBL> reset(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		this.automaton = automaton;
		this.gfpa      = automaton.getGFPA();
		this.element   = element;
		return this;
	}

	// ==========================================================================

	public Collection<IFSAState<VAL, LBL>> matches()
	{
		MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap   = new ArrayListValuedHashMap<>();
		List<Collection<IFSAState<VAL, LBL>>>                childsSubStates = new ArrayList<>();

		Iterator<INode<VAL, LBL>> nodes = BUFTAMatches.processLeaves(gfpa, element, //
			(node, states) -> nodeStatesMap.putAll(node, states));

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

			if (!nodes.hasNext())
				return childsSubStates.stream().flatMap(l -> l.stream()).collect(Collectors.toSet());

			Collection<IFSAState<VAL, LBL>> newStates = new HashSet<>();

			// Check hyper transitions
			for (IFTAEdge<VAL, LBL> hEdge : automaton.getFTAEdges(childsSubStates))
			{
				if (!hEdge.getCondition().testND(childsSubStates))
					continue;

				IFSAState<VAL, LBL> newState = hEdge.getChild();
				newStates.addAll(IGFPA.getValidStates(gfpa, newState, stateOnNodePredicate));
			}
			nodeStatesMap.putAll(node, newStates);
			childsSubStates.clear();
		}
		return nodeStatesMap.values();
	}
}