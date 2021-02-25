package insomnia.rule.dependency.condition;

import java.util.Collections;

import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependencyCondition;

public final class VocabularyBetaCondition<V, E> implements IDependencyCondition<V, E>
{
	@Override
	public boolean test(IRule<V, E> t, IRule<V, E> u)
	{
		return !Collections.disjoint(t.getBody().getVocabulary(), u.getHead().getVocabulary());
	}
}
