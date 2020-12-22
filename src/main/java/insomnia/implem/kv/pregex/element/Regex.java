package insomnia.implem.kv.regex.element;

/**
 * Regex element (delimited by ~)
 * Two different regex can have intersection area,
 * the user must take care of their use because of the determinization algorithm.
 */
public class Regex extends AbstractElement
{
	String regex;
	
	public Regex(String regex)
	{
		super();
		this.regex = regex;
	}
	
	public String getRegex()
	{
		return regex;
	}
	
	public String toString()
	{
		return regex + quantifier;
	}
}
