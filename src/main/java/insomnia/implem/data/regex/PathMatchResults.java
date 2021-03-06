package insomnia.implem.data.regex;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatchResult;
import insomnia.implem.data.Paths;

public final class PathMatchResults
{
	private PathMatchResults()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static <VAL, LBL> IPathMatchResult<VAL, LBL> create(IPath<VAL, LBL> group)
	{
		return new PathMatchResult<>(group, Paths.empty());
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> IPathMatchResult<VAL, LBL> empty()
	{
		return (IPathMatchResult<VAL, LBL>) TreeMatchResults.empty();
	}
}
