package insomnia.implem.data.regex;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatchResult;

public final class PathMatchResults
{
	private PathMatchResults()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static <VAL, LBL> IPathMatchResult<VAL, LBL> create(IPath<VAL, LBL> group)
	{
		return new PathMatchResult<>(group);
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> IPathMatchResult<VAL, LBL> empty()
	{
		return (IPathMatchResult<VAL, LBL>) TreeMatchResults.empty();
	}
}
