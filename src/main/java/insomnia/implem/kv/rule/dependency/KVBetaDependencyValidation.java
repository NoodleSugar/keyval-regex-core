package insomnia.implem.kv.rule.dependency;

import java.util.Arrays;
import java.util.Collection;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.unifier.KVPathUnifiers;
import insomnia.rule.IPathRule;
import insomnia.rule.dependency.AbstractBetaDependencyValidation;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.dependency.IDependencyCondition;
import insomnia.rule.dependency.condition.VocabularyBetaCondition;
import insomnia.unifier.IPathUnifier;
import insomnia.unifier.PathUnifiers;

public class KVBetaDependencyValidation extends AbstractBetaDependencyValidation<KVValue, KVLabel>
{

	public KVBetaDependencyValidation()
	{
		super(Arrays.asList( //
			new VocabularyBetaCondition<KVValue, KVLabel>() //
		));
	}

	public KVBetaDependencyValidation(Collection<IDependencyCondition<KVValue, KVLabel>> conditions)
	{
		super(conditions);
	}

	@Override
	public void setConditions(Collection<IDependencyCondition<KVValue, KVLabel>> conditions)
	{
		super.setConditions(conditions);
	}

	@Override
	protected PathUnifiers<KVValue, KVLabel> getPathUnifiers()
	{
		return KVPathUnifiers.get();
	}

	@Override
	protected IDependency<KVValue, KVLabel> newPathDependency(IPathUnifier<KVValue, KVLabel> unifier, IPathRule<KVValue, KVLabel> a, IPathRule<KVValue, KVLabel> b)
	{
		return new KVPathDependency(unifier, a, b);
	}
}
