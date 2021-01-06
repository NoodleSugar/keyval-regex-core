package insomnia.unifier;

import insomnia.data.ITree;

public interface IUnifier<V, E>
{
	ITree<V, E> getHead();

	ITree<V, E> getBody();

	ITree<V, E> getReference();

	boolean emptyBody();

	boolean emptyHead();
}
