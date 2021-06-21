package insomnia.implem.fsa.state;

import java.util.Collection;

import insomnia.fsa.IFSAMultiState;
import insomnia.fsa.IFSAState;

public final class FSAMultiStates
{
	private FSAMultiStates()
	{
		throw new AssertionError();
	}

	// ==========================================================================

	public static <VAL, LBL> IFSAMultiState<VAL, LBL> create(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return new FSAMultiState<VAL, LBL>(states);
	}
}
