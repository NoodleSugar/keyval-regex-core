package insomnia.implem.data.regex;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.implem.data.Trees;

public final class TreeMatchResults
{
	private TreeMatchResults()
	{
		throw new AssertionError();
	}

	// =========================================================================

	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> create(ITree<VAL, LBL> group)
	{
		return new TreeMatchResult<>(group);
	}

	private final static TreeMatchResult<?, ?> emptyResult = new TreeMatchResult<>(Trees.empty());

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> empty()
	{
		return (ITreeMatchResult<VAL, LBL>) emptyResult;
	}
}
