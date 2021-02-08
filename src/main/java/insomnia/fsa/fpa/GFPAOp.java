package insomnia.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public final class GFPAOp
{
	private GFPAOp()
	{
		throw new AssertionError();
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
