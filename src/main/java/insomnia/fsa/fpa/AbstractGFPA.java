package insomnia.fsa.fpa;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public abstract class AbstractGFPA<VAL, LBL> implements IGFPA<VAL, LBL>
{
	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state)
	{
		return getEdges(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> nextValidStates(IFSAState<VAL, LBL> state, IFPAPath<VAL, LBL> element)
	{
		return nextValidStates(Collections.singletonList(state), element);
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
}
