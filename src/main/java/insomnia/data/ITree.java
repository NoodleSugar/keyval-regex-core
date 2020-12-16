package insomnia.data;

import java.util.Collection;
import java.util.List;

public interface ITree<V, E>
{
	INode<V, E> getRoot();

	boolean isRooted();

	List<? extends IEdge<V, E>> getChildren(INode<V, E> node);

	IEdge<V, E> getParent(INode<V, E> node);

	Collection<E> getVocabulary();
}