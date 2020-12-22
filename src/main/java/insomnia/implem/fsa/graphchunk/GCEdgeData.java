package insomnia.implem.fsa.graphchunk;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import insomnia.implem.kv.data.KVValue;

public class GCEdgeData implements Predicate<Object>
{
	private Object obj;
	private Type   type;

	public enum Type
	{
		EPSILON, STRING_EQUALS, NUMBER, REGEX, //
		VALUE_EQUALS;
	};

	public static GCEdgeData createKVValue(KVValue value)
	{
		switch (value.getType())
		{
		case NUMBER:
			return createNumber(value.getNumber());
		case STRING:
			return createString(value.getString());
		case NULL:
		default:
			return createEpsilon();
		}
	}

	public static GCEdgeData createEpsilon()
	{
		return new GCEdgeData(Type.EPSILON, null);
	}

	public static GCEdgeData createString(String s)
	{
		return new GCEdgeData(Type.STRING_EQUALS, s);
	}

	public static GCEdgeData createRegex(String r)
	{
		return new GCEdgeData(Type.REGEX, r);
	}

	public static GCEdgeData createNumber(Number nb)
	{
		return new GCEdgeData(Type.NUMBER, nb);
	}

	public static GCEdgeData createValue(Object val)
	{
		return new GCEdgeData(Type.STRING_EQUALS, val);
	}

	public static GCEdgeData copy(GCEdgeData ref)
	{
		return new GCEdgeData(ref.type, ref.obj);
	}

	private GCEdgeData(Type type, Object obj)
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