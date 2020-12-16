package insomnia.rule.dependency;

import java.util.Collection;

import insomnia.rule.IRule;

public interface IDependencyValidation<V, E> extends IDependencyCondition<V, E>
{
	/**
	 * @return true if there is a dependency, false if there is not or if it can't compute it.
	 */
	@Override
	boolean test(IRule<V, E> t, IRule<V, E> u);

	Collection<IDependency<V, E>> getDependencies(IRule<V, E> a, IRule<V, E> b);
}
