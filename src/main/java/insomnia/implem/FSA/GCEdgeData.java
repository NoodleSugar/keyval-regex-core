package insomnia.implem.FSA;

public class GCEdgeData
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
	public String toString()
	{
		return type + ":" + obj;
	}
}