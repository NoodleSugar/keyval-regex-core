package insomnia.implem.fsa.fta.edgeCondition;

import java.util.Collections;
import java.util.List;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdgeCondition;

public class FTAEdgeConditions
{
	/**
	 * Create a state that must contain at least the 'parents' states.
	 */
	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> createInclusive(List<IFSAState<VAL, LBL>> states)
	{
		return new FTAInclusiveCondition<>(states);
	}

	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> createInclusive(IFSAState<VAL, LBL> state)
	{
		return new FTAInclusiveCondition<>(Collections.singletonList(state));
	}

	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> createEq(List<IFSAState<VAL, LBL>> states)
	{
		return new FTAEqualityCondition<>(states);
	}

	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> createSemiTwig(List<IFSAState<VAL, LBL>> states)
	{
		return new FTASemiTwigCondition<>(states);
	}

	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> copy(IFTAEdgeCondition<VAL, LBL> src, List<IFSAState<VAL, LBL>> states)
	{
		FTAAbstractCondition<VAL, LBL> ret = (FTAAbstractCondition<VAL, LBL>) copy(src);
		ret.setParentStates(states);
		return ret;
	}

	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> copy(IFTAEdgeCondition<VAL, LBL> src)
	{
		List<IFSAState<VAL, LBL>> states;

		if (!(src instanceof FTAAbstractCondition<?, ?>))
			throw new IllegalArgumentException();

		states = ((FTAAbstractCondition<VAL, LBL>) src).getParentStates();

		if (src instanceof FTAEqualityCondition<?, ?>)
			return createEq(states);
		if (src instanceof FTAInclusiveCondition<?, ?>)
			return createInclusive(states);
		if (src instanceof FTASemiTwigCondition<?, ?>)
			return createSemiTwig(states);
		throw new AssertionError();
	}
}
