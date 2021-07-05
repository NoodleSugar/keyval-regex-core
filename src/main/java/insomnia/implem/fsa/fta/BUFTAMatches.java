package insomnia.implem.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
	private interface ProcessLeaves<VAL, LBL>
	{
		boolean apply(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element, MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap);
	}

	private interface AfterEdgesCheck<VAL, LBL>
	{
		boolean apply(Iterator<INode<VAL, LBL>> nodes, List<Collection<IFSAState<VAL, LBL>>> childsSubStates_out);
	}
	// ==========================================================================

	private IBUFTA<VAL, LBL> automaton;
	private IGFPA<VAL, LBL>  gfpa;
	private ITree<VAL, LBL>  element;

	private ProcessLeaves<VAL, LBL>   processLeaves;
	private AfterEdgesCheck<VAL, LBL> afterEdgesCheck;

	private Collection<IFSAState<VAL, LBL>> retStates;

	// ==========================================================================

	private BUFTAMatches(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		this.automaton = automaton;
		this.gfpa      = automaton.getGFPA();
		this.element   = element;
	}

	public IBUFTA<VAL, LBL> getAutomaton()
	{
		return automaton;
	}

	public ITree<VAL, LBL> getElement()
	{
		return element;
	}

	public Collection<IFSAState<VAL, LBL>> getRetStates()
	{
		return retStates;
	}

	public static <VAL, LBL> BUFTAMatches<VAL, LBL> create(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		var ret = new BUFTAMatches<>(automaton, element);
		ret.processLeaves   = BUFTAMatches::normal_processLeaves;
		ret.afterEdgesCheck = (a, b) -> false;
		return ret;
	}

	public static <VAL, LBL> BUFTAMatches<VAL, LBL> createPreStates(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		var ret = new BUFTAMatches<>(automaton, element);
		ret.processLeaves   = BUFTAMatches::preState_processLeaves;
		ret.afterEdgesCheck = BUFTAMatches::preState_afterEdgesCheck;
		return ret;
	}

	// ==========================================================================

	private boolean hasFinal(Collection<IFSAState<VAL, LBL>> states)
	{
		return IterableUtils.matchesAny(states, gfpa::isFinal);
	}

	// ==========================================================================

	private static <VAL, LBL> boolean normal_processLeaves(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element, MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap)
	{
		var gfpa = automaton.getGFPA();
		return processLeaves(automaton.getGFPA(), element, //
			(node, states) -> //
			{
				if (IterableUtils.matchesAny(states, gfpa::isFinal))
					return true;

				nodeStatesMap.putAll(node, states);
				return false;
			});
	}

	private static <VAL, LBL> boolean preState_processLeaves(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element, MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap)
	{
		return BUFTAMatches.processLeaves(automaton.getGFPA(), element, //
			(node, states) -> nodeStatesMap.putAll(node, states));
	}

	private static <VAL, LBL> boolean preState_afterEdgesCheck(Iterator<INode<VAL, LBL>> nodes, List<Collection<IFSAState<VAL, LBL>>> childsSubStates_out)
	{
		if (!nodes.hasNext())
			return true;

		return false;
	}

	// ==========================================================================

	/**
	 * Process the leaves of the element.
	 * 
	 * @return the remaining nodes to process in the order of processing
	 */
	static <VAL, LBL> boolean processLeaves(IGFPA<VAL, LBL> gfpa, ITree<VAL, LBL> element, BiFunction<INode<VAL, LBL>, Collection<IFSAState<VAL, LBL>>, Boolean> consume)
	{
		for (var node : element.getLeaves())
		{
			for (var _class : IGFPA.getInitialClasses(gfpa, element, node))
				if (consume.apply(node, _class))
					return true;
		}
		return false;
	}

	private void finalizeResult(List<Collection<IFSAState<VAL, LBL>>> childsSubStates)
	{
		retStates = childsSubStates.stream().flatMap(Collection::stream).collect(Collectors.toSet());
	}

	public boolean matches()
	{
		retStates = Collections.emptySet();
		MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap = new ArrayListValuedHashMap<>();

		if (processLeaves.apply(automaton, element, nodeStatesMap))
			return true;

		var gfpa = automaton.getGFPA();

		List<Collection<IFSAState<VAL, LBL>>> childsSubStates = new ArrayList<>();
		ListIterator<INode<VAL, LBL>>         bottomUpNodes   = ITree.bottomUpOrder(element).listIterator();

		for (var node : ITree.bottomUpOrder_skipLeaves(element))
		{
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

			if (afterEdgesCheck.apply(bottomUpNodes, childsSubStates))
			{
				finalizeResult(childsSubStates);
				return true;
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
			{
				finalizeResult(childsSubStates);
				return true;
			}
		}
		finalizeResult(childsSubStates);
		return hasFinal(nodeStatesMap.get(element.getRoot()));
	}

}