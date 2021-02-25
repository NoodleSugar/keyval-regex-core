package insomnia.implem.kv.data;

public final class KVLabel
{
	private String label;

	KVLabel(String string)
	{
		label = string;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof KVLabel))
			return false;

		return label == ((KVLabel) obj).label //
			|| label.equals(((KVLabel) obj).label);
	}

	@Override
	public int hashCode()
	{
		if (label == null)
			return 0;
		return label.hashCode();
	}

	@Override
	public String toString()
	{
		return label;
	}
}
