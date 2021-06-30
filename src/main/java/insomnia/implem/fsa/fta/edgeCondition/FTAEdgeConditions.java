package insomnia.implem.fsa.fta.edgeCondition;

import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.fsa.fta.IFTAEdge.ConditionFactory;
import insomnia.fsa.fta.IFTAEdgeCondition;

public class FTAEdgeConditions
{
	private static ConditionFactory<?, ?> inclusion = //
		new ConditionFactory<>()
		{
			@Override
			public IFTAEdgeCondition<Object, Object> apply(IBUFTA<Object, Object> arg0, IFTAEdge<Object, Object> arg1)
			{
				return new FTAInclusiveCondition<>(arg0, arg1);
			}

			@Override
			public String toString()
			{
				return "∀";
			}
		};

	private static ConditionFactory<?, ?> equality = //
		new ConditionFactory<>()
		{
			@Override
			public IFTAEdgeCondition<Object, Object> apply(IBUFTA<Object, Object> arg0, IFTAEdge<Object, Object> arg1)
			{
				return new FTAEqualityCondition<>(arg0, arg1);
			}

			@Override
			public String toString()
			{
				return "=";
			}
		};

	private static ConditionFactory<?, ?> semiTwig = //
		new ConditionFactory<>()
		{
			@Override
			public IFTAEdgeCondition<Object, Object> apply(IBUFTA<Object, Object> arg0, IFTAEdge<Object, Object> arg1)
			{
				return new FTASemiTwigCondition<>(arg0, arg1);
			}

			@Override
			public String toString()
			{
				return "⧊";
			}
		};

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ConditionFactory<VAL, LBL> getInclusiveFactory()
	{
		return (ConditionFactory<VAL, LBL>) inclusion;
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ConditionFactory<VAL, LBL> getEqualityFactory()
	{
		return (ConditionFactory<VAL, LBL>) equality;
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ConditionFactory<VAL, LBL> getSemiTwigFactory()
	{
		return (ConditionFactory<VAL, LBL>) semiTwig;
	}
}
