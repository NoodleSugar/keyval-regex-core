package insomnia.automaton.state;

import java.util.ArrayList;

import insomnia.automaton.edge.IEdge;

public abstract class AState extends ArrayList<IEdge<String>> implements IState<String>
{
	private static final long serialVersionUID = -4201441333644144218L;
	protected int id;
	protected boolean isInitial;
	protected boolean isFinal;
	
	@Override
	public int getId()
	{
		return id;
	}

	@Override
	public boolean isInitial()
	{
		return isInitial;
	}

	@Override
	public boolean isFinal()
	{
		return isFinal;
	}
}
