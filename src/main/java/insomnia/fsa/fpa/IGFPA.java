package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSANodeCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;
import insomnia.implem.data.Trees;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.fsa.valuecondition.FSAValueConditions;

/**
 * Classic graph automaton representation.
 * 
 * @param <LBL>                   Type of the labels.
 * @param <IGFSAElement<VAL,LBL>> Type of the tested element.
 */
public interface IGFPA<VAL, LBL> extends IFPA<VAL, LBL>
{
	IFPAProperties getProperties();

	// =========================================================================

	boolean isInitial(IFSAState<VAL, LBL> state);

	boolean isFinal(IFSAState<VAL, LBL> state);

	boolean isRooted(IFSAState<VAL, LBL> state);

	boolean isTerminal(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> getStates();

	Collection<IFSAState<VAL, LBL>> getInitialStates();

	Collection<IFSAState<VAL, LBL>> getFinalStates();

	Collection<IFSAState<VAL, LBL>> getRootedStates();

	Collection<IFSAState<VAL, LBL>> getTerminalStates();

	void epsilonClosure(Collection<IFSAState<VAL, LBL>> states, VAL value);

	Collection<IFSAState<VAL, LBL>> getEpsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states, Predicate<IFSAState<VAL, LBL>> fcheckState);

	Collection<IFSAState<VAL, LBL>> getEpsilonClosure(IFSAState<VAL, LBL> state, Predicate<IFSAState<VAL, LBL>> fcheckState);

	Collection<IFSAState<VAL, LBL>> getEpsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states, VAL value);

	Collection<IFSAState<VAL, LBL>> getEpsilonClosure(IFSAState<VAL, LBL> state, VAL value);

	// =========================================================================

	/**
	 * @return all edges excluding epsilon transitions
	 */
	Collection<IFSAEdge<VAL, LBL>> getEdges();

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdges();

	Collection<IFSAEdge<VAL, LBL>> getAllEdges();

	Collection<IFSAEdge<VAL, LBL>> getEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdgesTo(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesTo(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesTo(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdgesOf(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesOf(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesOf(IFSAState<VAL, LBL> state);

	// =========================================================================

	public static <LBL> boolean testLabel(IFSALabelCondition<LBL> cond, LBL label)
	{
		return cond.test(label);
	}

	public static <VAL> boolean testValue(IFSAValueCondition<VAL> cond, VAL value)
	{
		return cond.test(value);
	}

	public static <VAL, LBL> boolean testNode(IFSANodeCondition<VAL, LBL> cond, ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		return cond.test(tree, node);
	}

	public static <VAL, LBL> boolean testStateOnNode(IFSAState<VAL, LBL> state, ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		return testValue(state.getValueCondition(), node.getValue()) && testNode(state.getNodeCondition(), tree, node);
	}

	public static <VAL, LBL> Predicate<IFSAState<VAL, LBL>> stateOnValuePredicate(VAL value)
	{
		return (s) -> testValue(s.getValueCondition(), value);
	}

	public static <VAL, LBL> Predicate<IFSAState<VAL, LBL>> stateOnNodePredicate(IGFPA<VAL, LBL> automaton, ITree<VAL, LBL> element, INode<VAL, LBL> node)
	{
		return (s) -> IGFPA.testStateOnNode(s, element, node);
	}

	// ==========================================================================

	public static <VAL, LBL> boolean hasAnyLoop(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return IterableUtils.matchesAny(automaton.getEdgesOf(state), e -> e.getChild() == state && FSALabelConditions.isAnyLoop(e.getLabelCondition()));
	}

	// ==========================================================================

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getInitials(IGFPA<VAL, LBL> automaton, VAL value)
	{
		var ret = CollectionUtils.select(automaton.getInitialStates(), s -> IGFPA.<VAL, LBL>stateOnValuePredicate(value).test(s));
		return automaton.getEpsilonClosure(ret, value);
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getInitials(IGFPA<VAL, LBL> automaton, ITree<VAL, LBL> element, INode<VAL, LBL> node)
	{
		return getValidStates(automaton, automaton.getInitialStates(), stateOnNodePredicate(automaton, element, node));
	}

	public static <VAL, LBL> Collection<Collection<IFSAState<VAL, LBL>>> getInitialClasses(IGFPA<VAL, LBL> automaton, ITree<VAL, LBL> element, INode<VAL, LBL> node)
	{
		Collection<Collection<IFSAState<VAL, LBL>>> classes = new ArrayList<>();

		for (var initial : automaton.getInitialStates())
			classes.add(getValidStates(automaton, initial, stateOnNodePredicate(automaton, element, node)));

		return classes;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getValidStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, Predicate<IFSAState<VAL, LBL>> fcheckState)
	{
		Collection<IFSAState<VAL, LBL>> ret = new HashSet<>();

		for (var state : states)
			ret.addAll(getValidStates(automaton, state, fcheckState));

		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getValidStates(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state, Predicate<IFSAState<VAL, LBL>> fcheckState)
	{
		if (!fcheckState.test(state))
			return Collections.emptyList();

		return automaton.getEpsilonClosure(state, fcheckState);
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getValidStates(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state, VAL value)
	{
		if (!IGFPA.testValue(state.getValueCondition(), value))
			return Collections.emptyList();

		return automaton.getEpsilonClosure(state, value);
	}

	public static <VAL, LBL> boolean isRooted(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return FSAValueConditions.createEq(Trees.getRootValue()).equals(state.getValueCondition());
	}

	public static <VAL, LBL> boolean isTerminal(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return FSAValueConditions.createEq(Trees.getTerminalValue()).equals(state.getValueCondition());
	}

	public static <VAL, LBL> IFSAState<VAL, LBL> skipRooted(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		var edges = automaton.getEdgesOf(state);
		assert edges.size() == 1;
		return edges.iterator().next().getChild();
	}

	public static <VAL, LBL> IFSAState<VAL, LBL> skipIfRooted(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		if (isRooted(automaton, state))
			return skipRooted(automaton, state);

		return state;
	}

	public static <VAL, LBL> IFSAState<VAL, LBL> skipTerminal(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		var edges = automaton.getEdgesTo(state);
		assert edges.size() == 1;
		return edges.iterator().next().getParent();
	}

	public static <VAL, LBL> IFSAState<VAL, LBL> skipIfTerminal(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		if (isTerminal(automaton, state))
			return skipTerminal(automaton, state);

		return state;
	}

	// ==========================================================================

	private static <VAL, LBL> void epsilonClosureTemplate( //
		IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, //
		BiFunction<IGFPA<VAL, LBL>, Collection<IFSAState<VAL, LBL>>, Collection<IFSAEdge<VAL, LBL>>> getEpsilonEdges, //
		Predicate<IFSAState<VAL, LBL>> fcheckState //
	)
	{
		if (states.isEmpty())
			return;

		int nbStates = automaton.getStates().size();

		Set<IFSAState<VAL, LBL>>  ret         = new HashSet<>(nbStates * 2);
		List<IFSAState<VAL, LBL>> buffStates  = new ArrayList<>(nbStates);
		List<IFSAState<VAL, LBL>> addedStates = new ArrayList<>(nbStates);

		buffStates.addAll(states);

		while (!buffStates.isEmpty())
		{
			for (IFSAEdge<VAL, LBL> edge : getEpsilonEdges.apply(automaton, buffStates))
			{
				var newState = edge.getChild();

				if (IFSALabelCondition.isEpsilon(edge.getLabelCondition()) && fcheckState.test(newState) && !ret.contains(newState))
					addedStates.add(newState);
			}
			buffStates.clear();
			buffStates.addAll(addedStates);
			ret.addAll(addedStates);
			addedStates.clear();
		}
		states.addAll(ret);
	}

	public static <VAL, LBL> void epsilonClosureOf(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		epsilonClosureTemplate(automaton, states, (a, e) -> a.getEpsilonEdgesOf(e), (s) -> true);
	}

	public static <VAL, LBL> void epsilonClosureOf(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, VAL value)
	{
		epsilonClosureTemplate(automaton, states, (a, e) -> a.getEpsilonEdgesOf(e), stateOnValuePredicate(value));
	}

	public static <VAL, LBL> void epsilonClosureOf(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, Predicate<IFSAState<VAL, LBL>> fcheckState)
	{
		epsilonClosureTemplate(automaton, states, (a, e) -> a.getEpsilonEdgesOf(e), fcheckState);
	}

	public static <VAL, LBL> void epsilonClosureTo(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		epsilonClosureTemplate(automaton, states, (a, e) -> a.getEpsilonEdgesTo(e), (s) -> true);
	}

	public static <VAL, LBL> void allEpsilonClosure(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		epsilonClosureTemplate(automaton, states, (a, e) -> CollectionUtils.union(a.getEpsilonEdgesTo(e), a.getEpsilonEdgesOf(e)), (s) -> true);
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getEpsilonClosureOf(IGFPA<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states, Predicate<IFSAState<VAL, LBL>> fcheckState)
	{
		List<IFSAState<VAL, LBL>> ret = new ArrayList<>(states);
		epsilonClosureOf(automaton, ret, fcheckState);
		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getEpsilonClosureOf(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, VAL value)
	{
		List<IFSAState<VAL, LBL>> ret = new ArrayList<>(states);
		epsilonClosureOf(automaton, ret, value);
		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getEpsilonClosureOf(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		List<IFSAState<VAL, LBL>> ret = new ArrayList<>(states);
		epsilonClosureOf(automaton, ret);
		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getEpsilonClosureTo(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		List<IFSAState<VAL, LBL>> ret = new ArrayList<>(states);
		epsilonClosureTo(automaton, ret);
		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getAllEpsilonClosure(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		List<IFSAState<VAL, LBL>> ret = new ArrayList<>(states);
		allEpsilonClosure(automaton, ret);
		return ret;
	}
}
