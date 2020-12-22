package insomnia.implem.kv.pregex.element;

import insomnia.implem.kv.data.KVValue;

public class Value implements IElement
{
	KVValue value;

	public Value(KVValue value)
	{
		this.value = value;
	}

	public KVValue getValue()
	{
		return value;
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
