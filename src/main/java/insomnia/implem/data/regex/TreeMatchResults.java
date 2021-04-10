package insomnia.implem.data.regex;

import java.util.Map;

import insomnia.data.INode;
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

	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> create(ITree<VAL, LBL> group, ITree<VAL, LBL> original, Map<INode<VAL, LBL>, INode<VAL, LBL>> nodeToOriginal)
	{
		return new TreeMatchResult<>(group, original, nodeToOriginal);
	}

	private final static ITreeMatchResult<?, ?> emptyResult = new PathMatchResult<>(Paths.empty(), Paths.empty());

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> empty()
	{
		return (ITreeMatchResult<VAL, LBL>) emptyResult;
	}
}
