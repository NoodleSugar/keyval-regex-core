package insomnia.implem.data.regex;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;

class TreeMatchResult<VAL, LBL> //
	extends AbstractMatchResult<VAL, LBL, ITree<VAL, LBL>> //
	implements ITreeMatchResult<VAL, LBL>
{
	TreeMatchResult(ITree<VAL, LBL> group)
	{
		super(group);
	}
}
