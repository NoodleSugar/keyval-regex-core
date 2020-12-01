package insomnia.data.tree.node;

import java.util.Optional;

import insomnia.data.tree.edge.IEdge;

public interface ITreeNode<E> extends INode<E>
{
	Optional<? extends IEdge<E>> getParent();
}
