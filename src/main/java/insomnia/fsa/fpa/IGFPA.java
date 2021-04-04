package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.data.IPath;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;

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

	/**
	 * Remove invalid final states from 'states' according to 'theElement'.
	 * 
	 * @param automaton  the automaton to process in
	 * @param states     the reached states from the automaton
	 * @param theElement the path to validate
	 */
	public static <VAL, LBL> void finalizeStates(IGFPA<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states, IPath<VAL, LBL> theElement)
	{
		if (!theElement.isTerminal())
			states.removeIf(state -> automaton.isTerminal(state));
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getInitials(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> theElement)
	{
		Stream<IFSAState<VAL, LBL>> ret = automaton.getInitialStates().stream();

		if (!theElement.isRooted())
			ret = ret.filter(s -> !automaton.isRooted(s));

		VAL value = theElement.getRoot().getValue();
		ret = ret.filter(statePredicate(value));
		return automaton.getEpsilonClosure(ret.collect(Collectors.toList()), value);
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> internalGetPersistentFinals(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		return CollectionUtils.select(states, s -> automaton.isFinal(s) && !automaton.isTerminal(s));
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> internalGetInitials(IGFPA<VAL, LBL> automaton, VAL value)
	{
		return automaton.getEpsilonClosure( //
			CollectionUtils.select(automaton.getInitialStates(), s -> !automaton.isRooted(s) && testValue(s.getValueCondition(), value)), //
			value);
	}

	/**
	 * In the processing of the automaton (not the first of the last step); check the validity of the final state 'state'.
	 * 
	 * @param automaton the automaton to process in
	 * @param state     the final state to check
	 * @return {@code true} if valid or {@code false}
	 */
	public static <VAL, LBL> boolean internalCheckFinalState(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		assert (automaton.isFinal(state));

		if (automaton.isTerminal(state))
			return false;

		return true;
	}

	public static <VAL, LBL> boolean internalCheckState(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		if (automaton.isTerminal(state))
			return false;

		return true;
	}

	/**
	 * In the processing of the automaton (not the first of the last step); check the validity of the initial state 'state'.
	 * 
	 * @param automaton the automaton to process in
	 * @param state     the initial state to check
	 * @return {@code true} if valid or {@code false}
	 */
	public static <VAL, LBL> boolean checkInitialState(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		assert (automaton.isInitial(state));

		if (automaton.isRooted(state))
			return false;

		return true;
	}

	public static <LBL> boolean testLabel(IFSALabelCondition<LBL> cond, LBL label)
	{
		return cond.test(label);
	}

	public static <VAL> boolean testValue(IFSAValueCondition<VAL> cond, VAL value)
	{
		return cond.test(value);
	}

	public static <VAL, LBL> Predicate<IFSAState<VAL, LBL>> statePredicate(VAL value)
	{
		return (s) -> testValue(s.getValueCondition(), value);
	}

	// ==========================================================================

	public static <VAL, LBL> void nextValidStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, LBL label, VAL value)
	{
		if (!states.isEmpty())
		{
			Collection<IFSAState<VAL, LBL>> persistentFinals = internalGetPersistentFinals(automaton, states);
			Collection<IFSAState<VAL, LBL>> buffStates       = new ArrayList<>(states);
			states.clear();

			for (IFSAEdge<VAL, LBL> edge : automaton.getEdgesOf(buffStates))
			{
				if (testLabel(edge.getLabelCondition(), label) && testValue(edge.getChild().getValueCondition(), value))
				{
					var newStates = automaton.getEpsilonClosure(edge.getChild(), value);
					states.addAll(newStates);
				}
			}
			states.addAll(persistentFinals);
		}
		states.addAll(internalGetInitials(automaton, value));
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getNextValidStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, LBL label, VAL value)
	{
		List<IFSAState<VAL, LBL>> ret = new ArrayList<>(states);
		nextValidStates(automaton, ret, label, value);
		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getNextValidStates(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(automaton.getStates().size() * 2);

		ret.addAll(getInitials(automaton, element));

		Iterator<LBL> labels = element.getLabels().iterator();
		Iterator<VAL> values = element.getValues().iterator();
		values.next();

		while (labels.hasNext())
		{
			nextValidStates(automaton, ret, labels.next(), values.next());
		}
		finalizeStates(automaton, ret, element);
		return ret;
	}

	// =========================================================================

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
		epsilonClosureTemplate(automaton, states, (a, e) -> a.getEpsilonEdgesOf(e), statePredicate(value));
	}

	public static <VAL, LBL> void epsilonClosureTo(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		epsilonClosureTemplate(automaton, states, (a, e) -> a.getEpsilonEdgesTo(e), (s) -> true);
	}

	public static <VAL, LBL> void allEpsilonClosure(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
	{
		epsilonClosureTemplate(automaton, states, (a, e) -> CollectionUtils.union(a.getEpsilonEdgesTo(e), a.getEpsilonEdgesOf(e)), (s) -> true);
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
