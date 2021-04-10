package insomnia.implem.data.regex;

import java.util.Map;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;

class TreeMatchResult<VAL, LBL> //
	extends AbstractMatchResult<VAL, LBL, ITree<VAL, LBL>> //
	implements ITreeMatchResult<VAL, LBL>
{
	TreeMatchResult(ITree<VAL, LBL> group, ITree<VAL, LBL> original, Map<INode<VAL, LBL>, INode<VAL, LBL>> nodeToOriginal)
	{
		super(group, original, nodeToOriginal);
	}
}
