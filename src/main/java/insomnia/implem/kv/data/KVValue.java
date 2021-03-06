package insomnia.implem.kv.data;

import org.apache.commons.lang3.ObjectUtils;

public final class KVValue
{
	public enum Type
	{
		NUMBER, STRING, NULL
	};

	private Type   type;
	private Object obj;

	KVValue()
	{
		type = Type.NULL;
		obj  = ObjectUtils.NULL;
	}

	KVValue(Number nb)
	{
		type = Type.NUMBER;
		obj  = nb;
	}

	KVValue(String s)
	{
		obj = s;
	}

	public Type getType()
	{
		return type;
	}

	public Number getNumber()
	{
		assert (type == Type.NUMBER);
		return (Number) obj;
	}

	public String getString()
	{
		assert (type == Type.STRING);
		return (String) obj;
	}

	@Override
	public String toString()
	{
		return obj.toString();
	}

	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof KVValue))
			return false;

		KVValue kvv = (KVValue) object;
		return type.equals(kvv.type) && obj.equals(kvv.obj);
	}

	@Override
	public int hashCode()
	{
		return type.hashCode() + obj.hashCode();
	}
}
