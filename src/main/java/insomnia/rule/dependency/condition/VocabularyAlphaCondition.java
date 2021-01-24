package insomnia.rule.dependency.condition;

import java.util.Collections;

import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependencyCondition;

public final class VocabularyAlphaCondition<VAL, LBL> implements IDependencyCondition<VAL, LBL>
{
	@Override
	public boolean test(IRule<VAL, LBL> t, IRule<VAL, LBL> u)
	{
		return !Collections.disjoint(t.getHead().getVocabulary(), u.getBody().getVocabulary());
	}
}
