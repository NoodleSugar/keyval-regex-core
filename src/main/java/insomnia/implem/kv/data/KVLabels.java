package insomnia.implem.kv.data;

import insomnia.fsa.factory.IFSALabelFactory;

public final class KVLabels
{
	private KVLabels()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static IFSALabelFactory<KVLabel> getFSALabelFactory()
	{
		return new IFSALabelFactory<KVLabel>()
		{
			@Override
			public KVLabel create(String label)
			{
				return KVLabels.create(label);
			}
		};
	}

	// =========================================================================

	public static KVLabel create(String label)
	{
		return new KVLabel(label);
	}
}
