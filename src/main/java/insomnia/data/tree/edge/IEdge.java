package insomnia.data.tree.edge;

import insomnia.data.tree.node.INode;

public interface IEdge<E>
{
	E getLabel();

	INode<E> getParent();

	INode<E> getChild();
}
