package insomnia.kv.regex.element;

public abstract class AbstractElement implements IElement
{
	Quantifier quantifier;
	
	@Override
	public void setQuantifier(Quantifier q)
	{
		quantifier = q;
	}
	
	@Override
	public Quantifier getQuantifier()
	{
		return quantifier;
	}
}
