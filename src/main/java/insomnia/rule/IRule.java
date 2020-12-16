package insomnia.rule;

import java.util.Collection;

import insomnia.data.ITree;

public interface IRule<V, E>
{
	ITree<V, E> getBody();

	ITree<V, E> getHead();

	/**
	 * Rooted for both body and head
	 */
	boolean isRooted();

	/**
	 * Body is terminal
	 */
	boolean isTerminal();

	/**
	 * Head leaf node is existential (ie. not frontier node)
	 */
	boolean isExistential();

	Collection<E> getVocabulary();
}
