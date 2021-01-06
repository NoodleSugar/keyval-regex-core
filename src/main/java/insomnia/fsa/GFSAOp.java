package insomnia.fsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GFSAOp
{
	private GFSAOp()
	{

	}

	// =========================================================================

	private static <VAL, LBL> void cleanBadStates(IGFSAutomaton<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, IFSAElement<VAL, LBL> theElement)
	{
		if (!theElement.isTerminal())
			states.removeIf(state -> automaton.isTerminal(state));

		states.removeIf(state -> false == state.getValueCondition().test(theElement.getValue().orElse(null)));
	}

	private static <VAL, LBL> boolean checkPreConditions(IGFSAutomaton<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states, IFSAElement<VAL, LBL> theElement)
	{
		if (states.isEmpty())
			return false;

		if (!theElement.isRooted())
			states.removeIf((state) -> automaton.isRooted(state));

		return true;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> nextValidState_sync(IGFSAutomaton<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states, IFSAElement<VAL, LBL> theElement)
	{
		Set<IFSAState<VAL, LBL>>        ret        = new HashSet<>(automaton.nbStates() * 2);
		Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(automaton.nbStates());

		ret.addAll(states);

		if (!checkPreConditions(automaton, ret, theElement))
			return Collections.emptyList();

		for (LBL element : theElement.getLabels())
		{
			if (ret.isEmpty())
				return Collections.emptyList();

			buffStates.addAll(ret);
			ret.clear();

			for (IFSAEdge<VAL, LBL> edge : automaton.getEdges(buffStates))
			{
				if (edge.getLabelCondition().test(element))
					ret.add(edge.getChild());
			}
			buffStates.clear();
		}
		cleanBadStates(automaton, ret, theElement);
		return new ArrayList<>(ret);
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> nextValidStates_general(IGFSAutomaton<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states, IFSAElement<VAL, LBL> theElement)
	{
		Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(automaton.nbStates() * 2);
		Collection<IFSAState<VAL, LBL>> buffStates;

		ret.addAll(states);

		if (!checkPreConditions(automaton, ret, theElement))
			return Collections.emptyList();

		for (LBL element : theElement.getLabels())
		{
			if (ret.isEmpty())
				return Collections.emptyList();

			buffStates = automaton.epsilonClosure(ret);
			ret.clear();

			for (IFSAEdge<VAL, LBL> edge : automaton.getEdges(buffStates))
			{
				if (edge.getLabelCondition().test(element))
					ret.add(edge.getChild());
			}
		}
		ret = automaton.epsilonClosure(ret);
		cleanBadStates(automaton, ret, theElement);
		return ret;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> epsilonClosure(IGFSAutomaton<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states)
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
