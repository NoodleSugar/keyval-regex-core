package insomnia.data.regex;

import org.apache.commons.collections4.BidiMap;

import insomnia.data.INode;
import insomnia.data.ITree;

public interface ITreeMatchResult<VAL, LBL>
{
	ITree<VAL, LBL> group();

	ITree<VAL, LBL> original();

	BidiMap<INode<VAL, LBL>, INode<VAL, LBL>> nodeToOriginal();
}
