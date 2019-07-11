package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

/**
 * Edge for words
 */
public class EdgeStringEqual extends Edge
{
	String strCmp;

	public EdgeStringEqual(IState<String> nextState, String strCmp)
	{
		super(nextState);
		this.strCmp = strCmp;
	}
	
	public String getWord()
	{
		return strCmp;
	}
	
	@Override
	public boolean isValid(String element)
	{
		return strCmp.equals(element);
	}
	
	@Override
	public String toString()
	{
		return " -> " + nextState + " : if equal " + strCmp;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof EdgeStringEqual)
		{
			EdgeStringEqual e = (EdgeStringEqual) o;
			if(e.nextState.equals(nextState) && e.strCmp.equals(strCmp))
				return true;
		}
		
		return false;
	}
}
