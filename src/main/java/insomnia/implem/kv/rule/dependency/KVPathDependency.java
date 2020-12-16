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

//	public static IPathUnifierFactory<KVValue, KVLabel, KVPathDependency> getFactory()
//	{
//		return new AbstractPathUnifierFactory<KVValue, KVLabel, KVPathDependency>()
//		{
//			@Override
//			public KVPathDependency get(IPath<KVValue, KVLabel> pb, IPath<KVValue, KVLabel> sb, IPath<KVValue, KVLabel> ph, IPath<KVValue, KVLabel> sh, IPath<KVValue, KVLabel> ref)
//			{
//				return new KVPathDependency(pb, sb, ph, sh, ref);
//			}
//		};
//	}

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
}
