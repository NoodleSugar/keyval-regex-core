package insomnia.implem.data.regex;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.data.regex.ITreeMatcher.ITreeBothResults;
import insomnia.implem.data.Paths;

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

	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> create(ITree<VAL, LBL> parent, INode<VAL, LBL> root, Iterable<? extends INode<VAL, LBL>> leaves)
	{
		return new TreeMatchResult<>(parent, root, leaves);
	}

	private final static ITreeMatchResult<?, ?> emptyResult = new PathMatchResult<>(Paths.empty());

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ITreeMatchResult<VAL, LBL> empty()
	{
		return (ITreeMatchResult<VAL, LBL>) emptyResult;
	}

	public static <VAL, LBL> ITreeBothResults<VAL, LBL> createBoth(ITreeMatchResult<VAL, LBL> standard, ITreeMatchResult<VAL, LBL> original)
	{
		return new ITreeBothResults<VAL, LBL>()
		{
			ITreeMatchResult<VAL, LBL> _standard = standard;
			ITreeMatchResult<VAL, LBL> _original = original;

			@Override
			public ITreeMatchResult<VAL, LBL> standard()
			{
				return _standard;
			}

			@Override
			public ITreeMatchResult<VAL, LBL> original()
			{
				return _original;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private final static ITreeBothResults<?, ?> emptyBoth = createBoth((ITreeMatchResult<Object, Object>) emptyResult, (ITreeMatchResult<Object, Object>) emptyResult);

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ITreeBothResults<VAL, LBL> emptyBoth()
	{
		return (ITreeBothResults<VAL, LBL>) emptyBoth;
	}
}
