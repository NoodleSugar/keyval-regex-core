package insomnia.unifier;

import java.util.Map;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.rule.IRule;

public interface ISemiTwigUnifier<VAL, LBL>
{
	IRule<VAL, LBL> rule();

	ITree<VAL, LBL> subHead();

	ITree<VAL, LBL> tree();

	ITree<VAL, LBL> treeSemiTwig();

	Map<INode<VAL, LBL>, INode<VAL, LBL>> treeToHead();

}
