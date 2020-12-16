package insomnia.implem.kv.unifier;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.unifier.PathUnifiers;

public final class KVPathUnifiers
{
	static PathUnifiers<KVValue, KVLabel> unifiers;

	static
	{
		try
		{
			unifiers = new PathUnifiers(KVPathUnifier.class);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static PathUnifiers<KVValue, KVLabel> get()
	{
		return unifiers;
	}
}
