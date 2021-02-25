package insomnia.rule.dependency;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;

import insomnia.rule.IPathRule;
import insomnia.rule.IRule;
import insomnia.unifier.IPathUnifier;
import insomnia.unifier.PathUnifiers;

public abstract class AbstractAlphaDependencyValidation<VAL, LBL> extends AbstractDependencyValidation<VAL, LBL>
{
	private PathUnifiers<VAL, LBL> pathUnifiers;

	public AbstractAlphaDependencyValidation(Collection<IDependencyCondition<VAL, LBL>> conditions, PathUnifiers<VAL, LBL> pathUnifiers)
	{
		super(conditions);
		this.pathUnifiers = pathUnifiers;
	}

	private PathUnifiers<VAL, LBL> getPathUnifiers()
	{
		return pathUnifiers;
	}

	abstract protected IDependency<VAL, LBL> newPathDependency(IPathUnifier<VAL, LBL> unifier, IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b);

	@Override
	public boolean test(IRule<VAL, LBL> a, IRule<VAL, LBL> b)
	{
		if (false == super.test(a, b))
			return false;

		if (a instanceof IPathRule && b instanceof IPathRule)
			return getPathUnifiers().haveUnifiers((IPathRule<VAL, LBL>) a, (IPathRule<VAL, LBL>) b);

		throw new NotImplementedException("");
	}

	@Override
	public Collection<IDependency<VAL, LBL>> getDependencies(IRule<VAL, LBL> a, IRule<VAL, LBL> b)
	{
		if (a instanceof IPathRule && b instanceof IPathRule)
		{
			Collection<IDependency<VAL, LBL>> alphas = new ArrayList<>();

			Collection<IPathUnifier<VAL, LBL>> unifiers = getPathUnifiers().compute((IPathRule<VAL, LBL>) a, (IPathRule<VAL, LBL>) b);

			for (IPathUnifier<VAL, LBL> unifier : unifiers)
				alphas.add(newPathDependency(unifier, (IPathRule<VAL, LBL>) a, (IPathRule<VAL, LBL>) b));

			return alphas;
		}
		throw new NotImplementedException("");
	}
}
