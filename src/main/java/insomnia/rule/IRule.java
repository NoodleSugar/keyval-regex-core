package insomnia.kv.rule;

import insomnia.data.tree.ITree;

public interface IRule<E extends ITree<?>>
{
	E getContext();

	E getBody();

	E getFoot();

	E getHead();

	/**
	 * Rooted for both body and head
	 */
	boolean isRooted();

	/**
	 * Body is rooted
	 */
	boolean isTerminal();
	
	boolean isExistential();
}
