package insomnia.automaton.state;

public class State extends AState
{
	private static final long serialVersionUID = -5397077735258042786L;

	public State(int id, boolean isInitial, boolean isFinal)
	{
		this.id = id;
		this.isInitial = isInitial;
		this.isFinal = isFinal;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == null)
			return false;
		if(o instanceof State)
		{
			State s = (State) o;
			if(s.getId() == id)
				return true;
		}
		return false;
	}

	@Override
	public String toString()
	{
		if(isFinal && isInitial)
			return "(" + id + "]";
		else if(isFinal)
			return "[" + id + "]";
		else if(isInitial)
			return "(" + id + ")";
		else
			return " " + id + " ";
	}
}
