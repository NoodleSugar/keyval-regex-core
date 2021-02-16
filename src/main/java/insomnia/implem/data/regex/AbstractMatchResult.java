package insomnia.implem.data.regex;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import insomnia.data.INode;
import insomnia.data.ITree;

abstract class AbstractMatchResult<VAL, LBL, RET extends ITree<VAL, LBL>>
{
	private RET group;

	AbstractMatchResult(RET group)
	{
		this.group = group;
	}

	public INode<VAL, LBL> start()
	{
		return group.getRoot();
	}

	public List<INode<VAL, LBL>> end()
	{
		throw new NotImplementedException("end()");
	}

	public RET group()
	{
		return group;
	}

	@Override
	public String toString()
	{
		return group.toString();
	}
}
