package insomnia.implem.data.regex;

import insomnia.data.ITree;

abstract class AbstractMatchResult<VAL, LBL, RET extends ITree<VAL, LBL>>
{
	private RET group, original;

	AbstractMatchResult(RET group, RET original)
	{
		this.group    = group;
		this.original = original;
	}

	public RET group()
	{
		return group;
	}

	public RET original()
	{
		return original;
	}

	@Override
	public String toString()
	{
		return group.toString();
	}
}
