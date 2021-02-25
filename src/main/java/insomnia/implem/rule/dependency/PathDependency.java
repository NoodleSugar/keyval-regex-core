package insomnia.implem.rule.dependency;

import insomnia.rule.IPathRule;
import insomnia.rule.IRule;
import insomnia.rule.dependency.IPathDependency;
import insomnia.unifier.IPathUnifier;
import insomnia.unifier.IUnifier;

final class PathDependency<VAL, LBL> implements IPathDependency<VAL, LBL>
{
	private IRule<VAL, LBL>        source;
	private IRule<VAL, LBL>        target;
	private IPathUnifier<VAL, LBL> unifier;

	public PathDependency(IPathUnifier<VAL, LBL> unifier, IPathRule<VAL, LBL> source, IPathRule<VAL, LBL> target)
	{
		this.source  = source;
		this.target  = target;
		this.unifier = unifier;
	}

	/**
	 * Get the beta path dependency from an alpha dependency.
	 * 
	 * @param alpha
	 * @return
	 */
	@Override
	public IUnifier<VAL, LBL> getUnifier()
	{
		return unifier;
	}

	@Override
	public IRule<VAL, LBL> getRuleSource()
	{
		return source;
	}

	@Override
	public IRule<VAL, LBL> getRuleTarget()
	{
		return target;
	}
}
