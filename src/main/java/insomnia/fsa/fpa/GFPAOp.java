package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
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

		states.removeIf(state -> false == state.getValueCondition().test(theElement.getLeaf().getValue()));
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

	public static <VAL, LBL> void nextValidStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, LBL label)
	{
		if (states.isEmpty())
			return;

		Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(states);
		states.clear();

		for (IFSAEdge<VAL, LBL> edge : automaton.getReachableEdges(buffStates))
		{
			if (edge.getLabelCondition().test(label))
				states.add(edge.getChild());
		}
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getNextValidStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, LBL label)
	{
		List<IFSAState<VAL, LBL>> ret = new ArrayList<>(states);
		nextValidStates(automaton, ret, label);
		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getNextValidStates(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(automaton.getStates().size() * 2);

		ret.addAll(automaton.getInitialStates());
		GFPAOp.initStates(automaton, ret, element);

		for (LBL label : element.getLabels())
			GFPAOp.nextValidStates(automaton, ret, label);

		automaton.epsilonClosure(ret);
		GFPAOp.finalizeStates(automaton, ret, element);
		return ret;
	}

	// =========================================================================

	public static <VAL, LBL> void epsilonClosure(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states)
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
			for (IFSAEdge<VAL, LBL> edge : automaton.getEpsilonEdgesOf(buffStates))
			{
				if (edge.getLabelCondition() == null && !ret.contains(edge.getChild()))
					addedStates.add(edge.getChild());
			}
			buffStates.clear();
			buffStates.addAll(addedStates);
			ret.addAll(addedStates);
			addedStates.clear();
		}
		states.addAll(ret);
	}
}
