package insomnia.implem.kv.unifier;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.rule.dependency.KVPathDependency;
import insomnia.unifier.PathUnifiers;

public final class KVPathUnifiers
{
	static PathUnifiers<KVValue, KVLabel, KVPathDependency> unifiers;

	static
	{
		try
		{
			unifiers = new PathUnifiers<KVValue, KVLabel, KVPathDependency>(KVPathDependency.class);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static PathUnifiers<KVValue, KVLabel, KVPathDependency> get()
	{
		return unifiers;
	}
}
