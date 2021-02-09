package insomnia.implem.data.regex;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;

class TreeMatchResult<VAL, LBL> implements ITreeMatchResult<VAL, LBL>
{
	private ITree<VAL, LBL> group;

	TreeMatchResult(ITree<VAL, LBL> group)
	{
		this.group = group;
	}

	@Override
	public INode<VAL, LBL> start()
	{
		return group.getRoot();
	}

	@Override
	public List<INode<VAL, LBL>> end()
	{
		throw new NotImplementedException("end()");
	}

	@Override
	public ITree<VAL, LBL> group()
	{
		return group;
	}

	@Override
	public String toString()
	{
		return group.toString();
	}
}
