package insomnia.implem.kv.data;

import java.text.NumberFormat;
import java.text.ParsePosition;

public final class KVValues
{
	private KVValues()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static KVValue mapValue(String svalue)
	{
		if (svalue == null)
			return null;
		if (svalue.isEmpty())
			return create();

		ParsePosition pos = new ParsePosition(0);
		Number        n   = NumberFormat.getInstance().parse(svalue, pos);

		if (pos.getIndex() == svalue.length())
			return create(n);

		return create(svalue);
	}

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