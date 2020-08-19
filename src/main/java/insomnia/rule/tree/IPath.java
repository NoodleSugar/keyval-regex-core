package insomnia.rule.tree;

import java.util.List;

public interface IPath<E> extends ITree<E>
{
	List<E> getLabels();

	int size();

	boolean isEmpty();

	boolean isTerminal();

	boolean isIncluded(IPath<E> path);

	boolean isPrefix(IPath<E> path);

	boolean isSuffix(IPath<E> path);

	boolean hasPrefixInSuffix(IPath<E> path);
}