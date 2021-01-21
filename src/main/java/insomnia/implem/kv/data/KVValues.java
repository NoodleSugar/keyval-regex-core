package insomnia.implem.kv.data;

public final class KVValues
{
	private KVValues()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static KVValue create()
	{
		return new KVValue();
	}

	public static KVValue create(Number n)
	{
		return new KVValue(n);
	}

	public static KVValue create(String s)
	{
		return new KVValue(s);
	}
}