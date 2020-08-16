package insomnia.rule.tree.edge;

import insomnia.rule.tree.node.INode;

public interface IEdge<E>
{
	E getLabel();

	INode<E> getParent();

	INode<E> getChild();
}
