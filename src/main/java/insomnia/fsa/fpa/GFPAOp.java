package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import insomnia.data.IPath;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;

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

		states.removeIf(state -> !testValue(state.getValueCondition(), theElement.getRoot().getValue()));
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

	public static <LBL> boolean testLabel(IFSALabelCondition<LBL> cond, LBL label)
	{
		return label == null || cond.test(label);
	}

	public static <VAL> boolean testValue(IFSAValueCondition<VAL> cond, VAL value)
	{
		return value == null || cond.test(value);
	}

	public static <VAL, LBL> void nextValidStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, LBL label, VAL value)
	{
		if (states.isEmpty())
			return;

		Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(states);
		states.clear();

		for (IFSAEdge<VAL, LBL> edge : automaton.getReachableEdges(buffStates))
		{
			if (testLabel(edge.getLabelCondition(), label) && testValue(edge.getChild().getValueCondition(), value))
				states.add(edge.getChild());
		}
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

		ret.addAll(automaton.getInitialStates());
		GFPAOp.initStates(automaton, ret, element);

		Iterator<LBL> labels = element.getLabels().iterator();
		Iterator<VAL> values = element.getValues().iterator();
		values.next(); // Skip the root

		while (labels.hasNext())
			GFPAOp.nextValidStates(automaton, ret, labels.next(), values.next());

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
