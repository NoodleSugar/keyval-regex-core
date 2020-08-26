package insomnia.automaton.state;

/*
 * Tree Automaton State for leaves
 */
public class ValueState extends AState implements IValueState<String>
{
	private static final long serialVersionUID = -7019488009802409318L;
	private Type type;
	private Double number;
	private String string;
	
	private ValueState(int id)
	{
		this.id = id;
		isInitial = true;
		isFinal = false;
	}
	
	public ValueState(int id, Double d)
	{
		this(id);
		type = Type.NUMBER;
		number = d;
	}
	
	public ValueState(int id, String s)
	{
		this(id);
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
