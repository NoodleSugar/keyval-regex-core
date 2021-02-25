package insomnia.implem.kv.data;

public final class KVLabels
{
	private KVLabels()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static KVLabel mapLabel(String slabel)
	{
		if (slabel == null)
			return null;

		return new KVLabel(slabel);
	}

	public static KVLabel create(String label)
	{
		return new KVLabel(label);
	}
}
