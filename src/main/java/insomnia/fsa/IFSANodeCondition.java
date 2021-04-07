package insomnia.fsa;

import insomnia.data.INode;
import insomnia.data.ITree;

public interface IFSANodeCondition<VAL, LBL>
{
	boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node);
}
