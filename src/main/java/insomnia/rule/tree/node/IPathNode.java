package insomnia.rule.tree.node;

import java.util.Optional;

import insomnia.rule.tree.edge.IEdge;

public interface IPathNode<E> extends ITreeNode<E>
{
	Optional<? extends IEdge<E>> getChild();
}
