package insomnia.regex.element;

/**
 * Union element
 */
public class OrElement extends MultipleElement
{
	public OrElement()
	{
		super();
	}
	
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		boolean hasQuantifier = quantifier.getInf() != 1 || quantifier.getSup() != 1;
		
		if(hasQuantifier)
			s.append('(');
		
		boolean first = true;
		for(IElement e : this)
		{
			if(first)
				first = false;
			else
				s.append('|');
			s.append(e);
		}
			
		if(hasQuantifier)
			s.append(')').append(quantifier);
		
		return s.toString();
	}
}
