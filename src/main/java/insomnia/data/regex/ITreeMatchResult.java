package insomnia.data.regex;

import java.util.List;

import insomnia.data.INode;
import insomnia.data.ITree;

public interface ITreeMatchResult<VAL, LBL>
{
	INode<VAL, LBL> start();

	List<INode<VAL, LBL>> end();

	ITree<VAL, LBL> group();
}
