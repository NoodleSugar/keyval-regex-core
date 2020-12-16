package insomnia.rule.dependency;

import java.util.function.BiPredicate;

import insomnia.rule.IRule;

/**
 * A necessary condition to have a dependency.
 * 
 * @author zuri
 * @param <V>
 * @param <E>
 */
public interface IDependencyCondition<V, E> extends BiPredicate<IRule<V, E>, IRule<V, E>>
{
	/**
	 * @return true if the necessary condition raise (it may have a dependency).
	 */
	@Override
	boolean test(IRule<V, E> t, IRule<V, E> u);
}
