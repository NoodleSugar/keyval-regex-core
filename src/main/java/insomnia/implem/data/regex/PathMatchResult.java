package insomnia.implem.data.regex;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatchResult;

class PathMatchResult<VAL, LBL> //
	extends AbstractMatchResult<VAL, LBL, IPath<VAL, LBL>> //
	implements IPathMatchResult<VAL, LBL>
{
	PathMatchResult(IPath<VAL, LBL> group, IPath<VAL, LBL> original)
	{
		super(group, original);
		this.nodeToOriginal.put(group.getLeaf(), original.getLeaf());
	}
}
