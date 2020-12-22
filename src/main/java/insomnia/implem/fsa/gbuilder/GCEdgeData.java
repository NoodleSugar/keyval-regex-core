package insomnia.implem.fsa.gbuilder;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class GCEdgeData implements Predicate<Object>
{
	private Object obj;
	private Type   type;

	public enum Type
	{
		EPSILON, STRING_EQUALS, NUMBER, REGEX;
	};

	public GCEdgeData(GCEdgeData data)
	{
		this.obj  = data.obj;
		this.type = data.type;
	}

	public GCEdgeData(Type type)
	{
		this.obj  = null;
		this.type = type;
	}

	public GCEdgeData(Object obj, Type type)
	{
		this.obj  = obj;
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}

	public Object getObj()
	{
		return obj;
	}

	@Override
	public boolean test(Object t)
	{
		switch (type)
		{
		case EPSILON:
			return t == null;
		case NUMBER:
		case STRING_EQUALS:
			return t.toString().equals(obj);
		case REGEX:
		default:
			return Pattern.matches((String) obj, t.toString());
		}
	}

	@Override
	public String toString()
	{
		return type + ":" + obj;
	}
}