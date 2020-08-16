package insomnia.rule.tree;

import java.util.List;

import insomnia.rule.tree.edge.IEdge;
import insomnia.rule.tree.node.INode;

public interface ITree<E>
{
	INode<E> getRoot();

	IEdge<E> getParent(INode<E> node);

	List<? extends IEdge<E>> getChildren(INode<E> node);

	boolean isRooted();
}