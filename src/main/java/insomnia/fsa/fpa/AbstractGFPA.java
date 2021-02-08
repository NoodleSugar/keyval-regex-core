package insomnia.fsa.fpa;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public abstract class AbstractGFPA<VAL, LBL> implements IGFPA<VAL, LBL>
{
	@Override
	public int nbEdges(IFSAState<VAL, LBL> state)
	{
		return nbEdges(Collections.singleton(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state)
	{
		return getEdges(Collections.singletonList(state));
	}

	// =========================================================================
	// Help

	@Override
	public boolean isInitial(IFSAState<VAL, LBL> state)
	{
		return getInitialStates().contains(state);
	}

	@Override
	public boolean isFinal(IFSAState<VAL, LBL> state)
	{
		return getFinalStates().contains(state);
	}

	// =========================================================================

	@Override
	public String toString()
	{
		StringBuffer s1 = new StringBuffer();

		s1.append("Initials: ").append(getInitialStates()).append("\n");
		s1.append("Finals: ").append(getFinalStates()).append("\n");
		s1.append("Rooted: ").append(getRootedStates()).append("\n");
		s1.append("Terminals: ").append(getTerminalStates()).append("\n");
		s1.append("States: ").append(getStates()).append("\n");
		s1.append("Edges:\n");

		for (IFSAEdge<VAL, LBL> edge : getEdges())
			s1.append(edge).append("\n");

		return s1.toString();
	}
}
