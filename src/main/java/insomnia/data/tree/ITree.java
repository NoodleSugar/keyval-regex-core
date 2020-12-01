package insomnia.data.tree;

import insomnia.data.tree.node.INode;

public interface ITree<E>
{
	INode<E> getRoot();

	boolean isRooted();
}