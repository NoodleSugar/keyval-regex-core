package insomnia.data.regex;

import java.util.List;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.implem.data.regex.TreeMatchResults;

public abstract class AbstractTreeMatcher<VAL, LBL> implements ITreeMatcher<VAL, LBL>
{
	protected ITreeMatchResult<VAL, LBL> matchResult;

	public AbstractTreeMatcher()
	{
		matchResult = TreeMatchResults.empty();
	}

	@Override
	public ITreeMatchResult<VAL, LBL> toMatchResult()
	{
		return matchResult;
	}

	@Override
	public INode<VAL, LBL> start()
	{
		return matchResult.start();
	}

	@Override
	public List<INode<VAL, LBL>> end()
	{
		return matchResult.end();
	}

	@Override
	public ITree<VAL, LBL> group()
	{
		return matchResult.group();
	}
}
