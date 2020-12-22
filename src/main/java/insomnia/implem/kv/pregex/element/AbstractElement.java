package insomnia.implem.kv.pregex.element;

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
