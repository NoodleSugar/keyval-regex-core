package insomnia.implem.fsa.fpa;

import insomnia.fsa.IFSAState;

class From
{
	private Object initialState;

	private int from;

	public From(Object initialState, int offset)
	{
		this.initialState = initialState;
		this.from         = offset;
	}

	// ==========================================================================

	public int getFrom()
	{
		return from;
	}

	@SuppressWarnings("unchecked")
	public <VAL, LBL> IFSAState<VAL, LBL> getInitialState()
	{
		return (IFSAState<VAL, LBL>) initialState;
	}

	// ==========================================================================

	@Override
	public int hashCode()
	{
		return initialState.hashCode() + from * 31;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof From))
			return false;

		From from = (From) obj;
		return initialState == from.initialState && this.from == from.from;
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("(").append(initialState).append(", ").append(from).append(")").toString();
	}
}
