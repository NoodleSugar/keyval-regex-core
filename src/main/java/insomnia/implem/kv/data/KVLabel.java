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

		return label.equals(((KVLabel) obj).label);
	}

	@Override
	public int hashCode()
	{
		return label.hashCode();
	}

	@Override
	public String toString()
	{
		return label;
	}
}
