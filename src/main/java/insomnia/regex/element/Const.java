package insomnia.regex.element;

public class Const implements IElement
{
	public String str;
	public double num;

	public Const(String value)
	{
		try
		{
			str = null;
			num = Double.parseDouble(value);
		}
		catch(NumberFormatException nfe)
		{
			str = value;
		}
	}

	public boolean isNumber()
	{
		return str == null;
	}

	@Override
	public Quantifier getQuantifier()
	{
		return null;
	}

	@Override
	public void setQuantifier(Quantifier q)
	{
	}

}
