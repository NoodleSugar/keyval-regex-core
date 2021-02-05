package insomnia.fsa.fpa;

import java.util.Collection;
import java.util.Collections;

import insomnia.data.IPath;
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

	@Override
	public Collection<IFSAState<VAL, LBL>> nextValidStates(IFSAState<VAL, LBL> state, IPath<VAL, LBL> element)
	{
		return nextValidStates(Collections.singletonList(state), element);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, IPath<VAL, LBL> theElement)
	{
		if (getProperties().isSynchronous())
			return GFPAOp.nextValidState_sync(this, states, theElement);
		else
			return GFPAOp.nextValidStates_general(this, states, theElement);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> epsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return GFPAOp.epsilonClosure(this, states);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> epsilonClosure(IFSAState<VAL, LBL> state)
	{
		return epsilonClosure(Collections.singletonList(state));
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
