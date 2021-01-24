package insomnia.implem.kv.data;

import insomnia.data.IPath;
import insomnia.implem.data.Paths;

public final class KVPaths
{
	private KVPaths()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static IPath<KVValue, KVLabel> pathFromString(String p)
	{
		return Paths.pathFromString(p, KVLabels::create);
	}

	public static IPath<KVValue, KVLabel> pathFromString(String p, KVValue value)
	{
		return Paths.pathFromString(p, value, KVLabels::create);
	}
}
