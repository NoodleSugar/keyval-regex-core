package insomnia.implem.data.regex;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import insomnia.data.INode;
import insomnia.data.ITree;

abstract class AbstractMatchResult<VAL, LBL, RET extends ITree<VAL, LBL>>
{
	private RET group, original;

	protected BidiMap<INode<VAL, LBL>, INode<VAL, LBL>> nodeToOriginal;

	AbstractMatchResult(RET group, RET original)
	{
		this(group, original, Collections.emptyMap());
	}

	AbstractMatchResult(RET group, RET original, Map<INode<VAL, LBL>, INode<VAL, LBL>> nodeToOriginal)
	{
		this.group          = group;
		this.original       = original;
		this.nodeToOriginal = new DualHashBidiMap<>(nodeToOriginal);
	}

	public RET group()
	{
		return group;
	}

	public RET original()
	{
		return original;
	}

	public BidiMap<INode<VAL, LBL>, INode<VAL, LBL>> nodeToOriginal()
	{
		return nodeToOriginal;
	}

	@Override
	public String toString()
	{
		return group.toString();
	}
}
