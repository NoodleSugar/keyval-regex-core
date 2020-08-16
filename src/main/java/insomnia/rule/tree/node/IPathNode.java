package insomnia.rule.tree.node;

import insomnia.rule.tree.edge.IEdge;

public interface IPathNode<E> extends ITreeNode<E>
{
	IEdge<E> getChild();
}
