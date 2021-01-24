package insomnia.implem.rule.dependency;

import java.util.Arrays;
import java.util.Collection;

import insomnia.rule.IPathRule;
import insomnia.rule.dependency.AbstractAlphaDependencyValidation;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.dependency.IDependencyCondition;
import insomnia.rule.dependency.condition.VocabularyAlphaCondition;
import insomnia.unifier.IPathUnifier;
import insomnia.unifier.PathUnifiers;

public class AlphaDependencyValidation<VAL, LBL> extends AbstractAlphaDependencyValidation<VAL, LBL>
{
	public AlphaDependencyValidation(PathUnifiers<VAL, LBL> pathUnifiers)
	{
		super(Arrays.asList( //
			new VocabularyAlphaCondition<VAL, LBL>() //
		), pathUnifiers);
	}

	public AlphaDependencyValidation(Collection<IDependencyCondition<VAL, LBL>> conditions, PathUnifiers<VAL, LBL> pathUnifiers)
	{
		super(conditions, pathUnifiers);
	}

	@Override
	protected IDependency<VAL, LBL> newPathDependency(IPathUnifier<VAL, LBL> unifier, IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b)
	{
		return new PathDependency<>(unifier, a, b);
	}
}
