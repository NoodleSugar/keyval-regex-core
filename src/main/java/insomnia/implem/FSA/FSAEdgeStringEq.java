package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

/**
 * Edge for words
 */
public class EdgeStringEqual extends Edge
{
	String strCmp;

	public EdgeStringEqual(IState<String> parent, IState<String> child, String strCmp)
	{
		super(parent, child);
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
		return parent + " -> " + child + " : if equal " + strCmp;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof EdgeStringEqual))
			return false;

		EdgeStringEqual e = (EdgeStringEqual) o;
		if(e.parent.equals(parent) && e.child.equals(child) && e.strCmp.equals(strCmp))
			return true;

		return false;
	}
}
