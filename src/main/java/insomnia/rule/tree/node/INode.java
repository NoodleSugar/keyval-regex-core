package insomnia.rule.tree.node;

import java.util.List;

import insomnia.rule.tree.edge.IEdge;

public interface INode<E>
{
	List<? extends IEdge<E>> getParents();

	List<? extends IEdge<E>> getChildren();
}
