package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

/**
 * Edge for epsilon transition
 */
public class EdgeEpsilon extends Edge
{
	public EdgeEpsilon(IState<String> parent, IState<String> child)
	{
		super(parent, child);
	}
	
	@Override
	public boolean isValid(String element)
	{
		return true;
	}
	
	public String toString()
	{
		return parent + " -> " + child + " : Empty";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof EdgeEpsilon)
			return true;
		return false;
	}
}
