package insomnia.rule.tree.value;

public class Value implements IValue
{
	private Type type;
	private Double number;
	private String string;

	public Value(Double d)
	{
		type = Type.NUMBER;
		number = d;
	}

	public Value(String s)
	{
		type = Type.STRING;
		string = s;
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	public Object getValue()
	{
		switch(type)
		{
		case NUMBER:
			return number;
		case STRING:
			return string;
		}
		return null;
	}
}
