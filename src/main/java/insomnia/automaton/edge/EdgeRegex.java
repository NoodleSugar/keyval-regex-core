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

	public EdgeRegex(IState<String> parent, IState<String> child, String regex)
	{
		super(parent, child);
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
		return parent + " -> " + child + " : if matches " + regex;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof EdgeStringEqual))
			return false;

		EdgeStringEqual e = (EdgeStringEqual) o;
		if(e.parent.equals(parent) && e.child.equals(child) && e.strCmp.equals(regex))
			return true;
		
		return false;
	}
}
