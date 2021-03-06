package insomnia.implem.kv.unifier;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.unifier.PathUnifier;
import insomnia.implem.unifier.PathUnifiers;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class KVPathUnifiers
{
	static PathUnifiers<KVValue, KVLabel> unifiers;

	static
	{
		try
		{
			unifiers = new PathUnifiers(PathUnifier.class);
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
