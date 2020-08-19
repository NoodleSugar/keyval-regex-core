package insomnia.rule.tree.node;

import java.util.Optional;

import insomnia.rule.tree.edge.IEdge;

public interface ITreeNode<E> extends INode<E>
{
	Optional<? extends IEdge<E>> getParent();
}
