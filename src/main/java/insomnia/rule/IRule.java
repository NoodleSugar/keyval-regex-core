package insomnia.rule;

import insomnia.rule.tree.ITree;

public interface IRule<E extends ITree>
{
	E getContext();
	E getBody();
	E getHead();
	
	boolean dependsOn(IRule<E> rule);
}
