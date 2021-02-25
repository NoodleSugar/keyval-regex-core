package insomnia.implem.kv.rule;

import java.text.ParseException;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPaths;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.rule.PathRules;
import insomnia.rule.IPathRule;

public final class KVPathRules
{
	private KVPathRules()
	{
		throw new AssertionError();
	}

	public static IPathRule<KVValue, KVLabel> fromString(String body, String head) throws ParseException
	{
		return PathRules.create(KVPaths.pathFromString(body), KVPaths.pathFromString(head));
	}

	public static IPathRule<KVValue, KVLabel> fromString(String body, String head, boolean isExistential) throws ParseException
	{
		return PathRules.create(KVPaths.pathFromString(body), KVPaths.pathFromString(head), isExistential);
	}
}
