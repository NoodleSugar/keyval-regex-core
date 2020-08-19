package insomnia.rule.tree;

import insomnia.rule.tree.node.INode;

public interface ITree<E>
{
	INode<E> getRoot();

	boolean isRooted();
}