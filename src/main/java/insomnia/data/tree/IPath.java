package insomnia.data.tree;

import java.util.List;

public interface IPath<E> extends ITree<E>
{
	List<E> getLabels();

	int size();

	boolean isEmpty();

	boolean isTerminal();

	default boolean isComplete()
	{
		return isRooted() && isTerminal();
	}

	default boolean isFree()
	{
		return !isRooted() && !isTerminal();
	}

	default boolean isFixed()
	{
		return isRooted() || isTerminal();
	}
}