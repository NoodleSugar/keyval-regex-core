package insomnia.fsa.fpa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public abstract class AbstractGFPA<VAL, LBL> implements IGFPA<VAL, LBL>
{
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

	@Override
	public boolean isRooted(IFSAState<VAL, LBL> state)
	{
		return getRootedStates().contains(state);
	}

	@Override
	public boolean isTerminal(IFSAState<VAL, LBL> state)
	{
		return getTerminalStates().contains(state);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getInitialStates()
	{
		return getStates().stream().filter(this::isInitial).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getFinalStates()
	{
		return getStates().stream().filter(this::isFinal).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getRootedStates()
	{
		return getStates().stream().filter(this::isRooted).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getTerminalStates()
	{
		return getStates().stream().filter(this::isTerminal).collect(Collectors.toList());
	}

	@Override
	public void epsilonClosure(Collection<IFSAState<VAL, LBL>> states)
	{
		if (getProperties().isSynchronous())
			return;

		GFPAOp.epsilonClosureOf(this, states);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IFSAState<VAL, LBL>> getEpsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		if (getProperties().isSynchronous())
			return (Collection<IFSAState<VAL, LBL>>) states;

		Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(states);
		epsilonClosure(ret);
		return ret;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getEpsilonClosure(IFSAState<VAL, LBL> state)
	{
		return getEpsilonClosure(Collections.singletonList(state));
	}

	// =========================================================================

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges()
	{
		return getAllEdges().stream().filter(edge -> !IFSAEdge.isEpsilon(edge)).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdges()
	{
		return getAllEdges().stream().filter(IFSAEdge::isEpsilon).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return getAllEdgesTo(states).stream().filter(edge -> !IFSAEdge.isEpsilon(edge)).collect(Collectors.toSet());
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdgesTo(IFSAState<VAL, LBL> state)
	{
		return getEdgesTo(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return getAllEdgesTo(states).stream().filter(IFSAEdge::isEpsilon).collect(Collectors.toSet());
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesTo(IFSAState<VAL, LBL> state)
	{
		return getEpsilonEdgesTo(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdgesTo(IFSAState<VAL, LBL> state)
	{
		return getAllEdgesTo(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return getAllEdgesOf(states).stream().filter(edge -> !IFSAEdge.isEpsilon(edge)).collect(Collectors.toSet());
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdgesOf(IFSAState<VAL, LBL> state)
	{
		return getEdgesOf(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return getAllEdgesOf(states).stream().filter(IFSAEdge::isEpsilon).collect(Collectors.toSet());
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesOf(IFSAState<VAL, LBL> state)
	{
		return getEpsilonEdgesOf(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdgesOf(IFSAState<VAL, LBL> state)
	{
		return getAllEdgesOf(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getReachableEdges(IFSAState<VAL, LBL> state)
	{
		return getReachableEdges(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getReachableEdges(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return getEdgesOf(getEpsilonClosure(states));
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

		for (IFSAState<VAL, LBL> state : getStates())
		{
			for (IFSAEdge<VAL, LBL> edge : getAllEdgesOf(state))
				s1.append(edge).append("\n");
		}
		return s1.toString();
	}
}
