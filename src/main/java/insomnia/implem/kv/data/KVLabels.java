package insomnia.implem.kv.data;

import insomnia.fsa.factory.ILabelFactory;

public final class KVLabels
{
	private KVLabels()
	{
	}

	public static ILabelFactory<KVLabel> getFactory()
	{
		return new ILabelFactory<KVLabel>()
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
