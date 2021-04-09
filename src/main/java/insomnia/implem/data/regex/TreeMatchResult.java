package insomnia.implem.data.regex;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.implem.data.Trees;

class TreeMatchResult<VAL, LBL> //
	extends AbstractMatchResult<VAL, LBL, ITree<VAL, LBL>> //
	implements ITreeMatchResult<VAL, LBL>
{
	TreeMatchResult(ITree<VAL, LBL> group)
	{
		super(group);
	}

	TreeMatchResult(ITree<VAL, LBL> parent, INode<VAL, LBL> root, Iterable<? extends INode<VAL, LBL>> leaves)
	{
		super(Trees.subTree(parent, root, leaves));
	}
}
