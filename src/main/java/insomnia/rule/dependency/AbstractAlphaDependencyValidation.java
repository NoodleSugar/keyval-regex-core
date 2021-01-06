package insomnia.rule.dependency;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;

import insomnia.rule.IPathRule;
import insomnia.rule.IRule;
import insomnia.unifier.IPathUnifier;
import insomnia.unifier.PathUnifiers;

public abstract class AbstractAlphaDependencyValidation<V, E> extends AbstractDependencyValidation<V, E>
{
	private PathUnifiers<V, E> pathUnifiers;

	public AbstractAlphaDependencyValidation(Collection<IDependencyCondition<V, E>> conditions, PathUnifiers<V, E> pathUnifiers)
	{
		super(conditions);
		this.pathUnifiers = pathUnifiers;
	}

	private PathUnifiers<V, E> getPathUnifiers()
	{
		return pathUnifiers;
	}

	abstract protected IDependency<V, E> newPathDependency(IPathUnifier<V, E> unifier, IPathRule<V, E> a, IPathRule<V, E> b);

	@Override
	public boolean test(IRule<V, E> a, IRule<V, E> b)
	{
		if (false == super.test(a, b))
			return false;

		if (a instanceof IPathRule && b instanceof IPathRule)
			return getPathUnifiers().haveUnifiers((IPathRule<V, E>) a, (IPathRule<V, E>) b);

		throw new NotImplementedException("");
	}

	@Override
	public Collection<IDependency<V, E>> getDependencies(IRule<V, E> a, IRule<V, E> b)
	{
		if (a instanceof IPathRule && b instanceof IPathRule)
		{
			Collection<IDependency<V, E>> alphas = new ArrayList<>();

			Collection<IPathUnifier<V, E>> unifiers = getPathUnifiers().compute((IPathRule<V, E>) a, (IPathRule<V, E>) b);

			for (IPathUnifier<V, E> unifier : unifiers)
				alphas.add(newPathDependency(unifier, (IPathRule<V, E>) a, (IPathRule<V, E>) b));

			return alphas;
		}
		throw new NotImplementedException("");
	}
}
