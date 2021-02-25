package insomnia.data.regex;

import insomnia.data.IPath;

public interface IPathMatchResult<VAL, LBL> extends ITreeMatchResult<VAL, LBL>
{
	@Override
	IPath<VAL, LBL> group();
}
