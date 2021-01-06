package insomnia.fsa;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractGFSAutomaton<VAL, LBL> implements IGFSAutomaton<VAL, LBL>
{
	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state)
	{
		return getEdges(Collections.singletonList(state));
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> nextValidStates(IFSAState<VAL, LBL> state, IFSAElement<VAL, LBL> element)
	{
		return nextValidStates(Collections.singletonList(state), element);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> epsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return GFSAOp.epsilonClosure(this, states);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> epsilonClosure(IFSAState<VAL, LBL> state)
	{
		return epsilonClosure(Collections.singletonList(state));
	}
}
