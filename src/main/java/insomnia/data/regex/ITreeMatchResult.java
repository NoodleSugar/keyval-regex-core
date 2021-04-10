package insomnia.data.regex;

import insomnia.data.ITree;

public interface ITreeMatchResult<VAL, LBL>
{
	ITree<VAL, LBL> group();

	ITree<VAL, LBL> original();
}
