package insomnia.rule.tree;

import java.util.List;

import insomnia.rule.tree.node.ITreeNode;

public interface ITree<E>
{
	ITreeNode<E> getRoot();

	List<? extends ITreeNode<E>> getLeaves();

	boolean isRooted();
}