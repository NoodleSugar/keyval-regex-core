package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import insomnia.data.IPath;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public final class GFPAOp
{
	private GFPAOp()
	{
		throw new AssertionError();
	}

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

		states.removeIf(state -> false == state.getValueCondition().test(theElement.getValue().orElse(null)));
	}

	/**
	 * Remove invalid initial states from 'states' according to 'theElement'.
	 * 
	 * @param automaton  the automaton to process in
	 * @param states     the reached states from the automaton
	 * @param theElement the path to validate
	 */
	public static <VAL, LBL> void initStates(IGFPA<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states, IPath<VAL, LBL> theElement)
	{
		if (!theElement.isRooted())
			states.removeIf((state) -> automaton.isRooted(state));
	}

	/**
	 * In the processing of the automaton (not the first of the last step); check the validity of the final state 'state'.
	 * 
	 * @param automaton the automaton to process in
	 * @param state     the final state to check
	 * @return {@code true} if valid or {@code false}
	 */
	public static <VAL, LBL> boolean checkFinalState(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		assert (automaton.isFinal(state));

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

	// =========================================================================

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> epsilonClosure(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return epsilonClosure(automaton, Collections.singleton(state));
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> epsilonClosure(IGFPA<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<IFSAState<VAL, LBL>>  ret         = new HashSet<>(automaton.nbStates() * 2);
		List<IFSAState<VAL, LBL>> buffStates  = new ArrayList<>(automaton.nbStates());
		List<IFSAState<VAL, LBL>> addedStates = new ArrayList<>(automaton.nbStates());

		ret.addAll(states);
		buffStates.addAll(states);

		while (!buffStates.isEmpty())
		{
			for (IFSAEdge<VAL, LBL> edge : automaton.getEdges(buffStates))
			{
				if (edge.getLabelCondition().test() && !ret.contains(edge.getChild()))
					addedStates.add(edge.getChild());
			}
			buffStates.clear();
			buffStates.addAll(addedStates);
			ret.addAll(addedStates);
			addedStates.clear();
		}
		return new ArrayList<>(ret);
	}
}
