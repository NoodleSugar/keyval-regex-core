package insomnia.rule;

import insomnia.rule.tree.ITree;

public interface IRule<E extends ITree<?>>
{
	E getContext();

	E getBody();

	E getFoot();

	E getHead();

	boolean isRooted();

	boolean isValued();
	
	boolean isExistential();
}
