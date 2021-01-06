package insomnia.rule.dependency;

import insomnia.rule.IRule;
import insomnia.unifier.IUnifier;

/**
 * A dependency between 2 rules, associated with a unique unifier.
 * 
 * @author zuri
 * @param <V>
 * @param <E>
 */
public interface IDependency<V, E>
{
	IUnifier<V, E> getUnifier();

	IRule<V, E> getRuleSource();

	IRule<V, E> getRuleTarget();
}
