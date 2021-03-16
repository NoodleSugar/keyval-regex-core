package insomnia.implem.kv.data;

import java.text.ParseException;

import insomnia.data.IPath;
import insomnia.implem.data.Paths;
import insomnia.implem.data.regex.parser.RegexParser;

public final class KVPaths
{
	private KVPaths()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static IPath<KVValue, KVLabel> pathFromString(String p) throws ParseException
	{
		RegexParser parser = new RegexParser("''\"\"~~");
		return Paths.pathFromPRegexElement(parser.parse(p), KVValues::mapValue, KVLabels::mapLabel);
	}
}
