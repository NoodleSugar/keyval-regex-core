package insomnia.implem.kv.data;

public final class KVLabels
{
	private KVLabels()
	{
	}

	// =========================================================================

	public static KVLabel create(String label)
	{
		return new KVLabel(label);
	}
}
