package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

/**
 * Edge for epsilon transition
 */
public class EdgeEpsilon extends Edge
{	
	public EdgeEpsilon(IState<String> nextState)
	{
		super(nextState);
	}
	
	@Override
	public boolean isValid(String element)
	{
		return true;
	}
	
	public String toString()
	{
		return " -> " + nextState + " : Empty";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof EdgeEpsilon)
			return true;
		return false;
	}
}
