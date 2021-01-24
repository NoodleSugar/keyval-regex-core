package insomnia.rule.dependency;

import java.util.ArrayList;
import java.util.Collection;

import insomnia.rule.IRule;

/**
 * A validation parameterized by some {@link IDependencyCondition}s.
 * 
 * @author zuri
 * @param <V>
 * @param <E>
 */
public abstract class AbstractDependencyValidation<V, E> implements IDependencyValidation<V, E>
{
	private Collection<IDependencyCondition<V, E>> conditions;

	public AbstractDependencyValidation(Collection<IDependencyCondition<V, E>> conditions)
	{
		this.conditions = new ArrayList<>(conditions);
	}

	protected Collection<IDependencyCondition<V, E>> getConditions()
	{
		return conditions;
	}

	@Override
	public boolean test(IRule<V, E> a, IRule<V, E> b)
	{
		for (IDependencyCondition<V, E> condition : conditions)
		{
			if (false == condition.test(a, b))
				return false;
		}
		return true;
	}
}
