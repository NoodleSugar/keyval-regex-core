package insomnia.data;

import java.util.List;

public interface IPath<V, E> extends ITree<V, E>
{
	IPath<V, E> subPath(int begin, int end);

	IPath<V, E> setValue(V value);

	List<E> getLabels();

	V getValue();

	int nbLabels();

	int size();

	boolean isEmpty();

	boolean isTerminal();

	boolean isComplete();

	boolean isFree();

	boolean isFixed();
}