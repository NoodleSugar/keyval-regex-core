package insomnia.implem.data.regex;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.implem.data.Paths;

public final class TreeMatchResults
{
	private TreeMatchResults()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> create(ITree<VAL, LBL> group, ITree<VAL, LBL> original)
	{
		return new TreeMatchResult<>(group, original);
	}

	private final static ITreeMatchResult<?, ?> emptyResult = new PathMatchResult<>(Paths.empty(), Paths.empty());

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> empty()
	{
		return (ITreeMatchResult<VAL, LBL>) emptyResult;
	}
}
