package insomnia.data.tree.node;

import java.util.List;

import insomnia.data.tree.edge.IEdge;

public interface INode<E>
{
	List<? extends IEdge<E>> getParents();

	List<? extends IEdge<E>> getChildren();
}
