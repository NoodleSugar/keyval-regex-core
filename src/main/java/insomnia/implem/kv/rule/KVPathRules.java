package insomnia.implem.kv.rule;

import insomnia.implem.data.Paths;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVLabels;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.rule.PathRules;
import insomnia.rule.IPathRule;

public final class KVPathRules
{
	private KVPathRules()
	{
		throw new AssertionError();
	}

	public static IPathRule<KVValue, KVLabel> fromString(String body, String head)
	{
		return PathRules.create(Paths.pathFromString(body, KVLabels::create), Paths.pathFromString(head, KVLabels::create));
	}

	public static IPathRule<KVValue, KVLabel> fromString(String body, String head, boolean isExistential)
	{
		return PathRules.create(Paths.pathFromString(body, KVLabels::create), Paths.pathFromString(head, KVLabels::create), isExistential);
	}
}
