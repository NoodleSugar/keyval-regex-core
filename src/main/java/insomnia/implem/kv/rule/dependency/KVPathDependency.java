package insomnia.implem.kv.rule.dependency;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.unifier.KVPathUnifier;
import insomnia.rule.IPathRule;
import insomnia.rule.IRule;
import insomnia.rule.dependency.IPathDependency;
import insomnia.unifier.IPathUnifier;
import insomnia.unifier.IUnifier;

public class KVPathDependency extends KVPathUnifier implements IPathDependency<KVValue, KVLabel>, IKVDependency<KVValue, KVLabel>
{
	IRule<KVValue, KVLabel> source;
	IRule<KVValue, KVLabel> target;

	public KVPathDependency(IPathUnifier<KVValue, KVLabel> unifier, IPathRule<KVValue, KVLabel> source, IPathRule<KVValue, KVLabel> target)
	{
		super(unifier);
		this.source = source;
		this.target = target;
	}

	/**
	 * Get the beta path dependency from an alpha dependency.
	 * 
	 * @param alpha
	 * @return
	 */
	@Override
	public IUnifier<KVValue, KVLabel> getUnifier()
	{
		return this;
	}

	@Override
	public IRule<KVValue, KVLabel> getSource()
	{
		return source;
	}

	@Override
	public IRule<KVValue, KVLabel> getTarget()
	{
		return target;
	}

	@Override
	public IRule<KVValue, KVLabel> getRuleSource()
	{
		return source;
	}

	@Override
	public IRule<KVValue, KVLabel> getRuleTarget()
	{
		return target;
	}
}
