package insomnia.automaton.edge;

import java.util.regex.Pattern;

import insomnia.automaton.state.IState;

/**
 * Edge for regex
 */
public class EdgeRegex extends Edge
{
	String regex;
	Pattern pattern;

	public EdgeRegex(IState<String> nextState, String regex)
	{
		super(nextState);
		this.regex = regex;
		pattern = Pattern.compile(regex);
	}
	
	@Override
	public boolean isValid(String element)
	{
		return pattern.matcher(element).matches();
	}
	
	@Override
	public String toString()
	{
		return " -> " + nextState + " : if matches " + regex;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof EdgeStringEqual)
		{
			EdgeStringEqual e = (EdgeStringEqual) o;
			if(e.nextState.equals(nextState) && e.strCmp.equals(regex))
				return true;
		}
		
		return false;
	}
}
