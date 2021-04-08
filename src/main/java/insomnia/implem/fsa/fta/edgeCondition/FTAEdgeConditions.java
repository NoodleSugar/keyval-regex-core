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
}
