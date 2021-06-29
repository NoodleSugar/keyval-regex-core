package insomnia.dataguide;

import java.util.EnumSet;
import java.util.List;

import insomnia.data.ITree;

public interface IClassicDataGuide<VAL, LBL> extends IDataGuide<VAL, LBL>
{
	ITree<EnumSet<NodeType>, LBL> getTree();

	// =========================================================================

	static public <VAL, LBL> EnumSet<NodeType> getType(IClassicDataGuide<VAL, LBL> dguide, List<LBL> path)
	{
		var ret = ITree.followUniquePath(dguide.getTree(), path);

		if (null == ret)
			return EnumSet.noneOf(NodeType.class);

		return ret;
	}
}
