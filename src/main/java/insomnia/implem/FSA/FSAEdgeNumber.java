package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

public class NumberEdge extends Edge
{
	double num;

	protected NumberEdge(IState<String> parent, IState<String> child, double num)
	{
		super(parent, child);
	}

	@Override
	public boolean isValid(String element)
	{
		try
		{
			return Double.compare(num, Double.parseDouble(element)) == 0;
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
	}

}
