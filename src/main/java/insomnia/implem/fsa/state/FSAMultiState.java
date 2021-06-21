package insomnia.implem.fsa.state;

import java.util.HashSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

import insomnia.fsa.IFSAMultiState;
import insomnia.fsa.IFSAState;

public final class FSAMultiState<VAL, LBL> extends HashSet<IFSAState<VAL, LBL>> implements IFSAMultiState<VAL, LBL>
{
	private static final int  defaultSize      = 16;
	private static final long serialVersionUID = 1L;

	public FSAMultiState(int size)
	{
		super(size);
	}

	public FSAMultiState(IFSAState<VAL, LBL> state)
	{
		super(defaultSize);
		add(state);
	}

	public FSAMultiState(Iterable<IFSAState<VAL, LBL>> states)
	{
		super(IterableUtils.size(states));
		CollectionUtils.addAll(this, states);
	}
}
