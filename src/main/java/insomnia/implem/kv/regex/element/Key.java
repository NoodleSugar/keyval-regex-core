package insomnia.implem.kv.regex.element;

/**
 * Word element
 */
public class Key extends AbstractElement
{
	String label;
	
	public Key(String label)
	{
		super();
		this.label = label;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public String toString()
	{
		return label + quantifier;
	}
}
