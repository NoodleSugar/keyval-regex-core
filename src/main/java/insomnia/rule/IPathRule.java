package insomnia.rule;

import insomnia.data.IPath;

public interface IPathRule<VAL, LBL> extends IRule<VAL, LBL>
{
	@Override
	IPath<VAL, LBL> getBody();

	@Override
	IPath<VAL, LBL> getHead();
}
