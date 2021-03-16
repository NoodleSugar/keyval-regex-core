package insomnia.fsa.fta;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import insomnia.fsa.IFSAState;

public interface IFTAEdgeCondition<VAL, LBL> extends Predicate<List<IFSAState<VAL, LBL>>>
{
	@Override
	boolean test(List<IFSAState<VAL, LBL>> states);

	/**
	 * Test a multi-state list for the edge condition.
	 */
	boolean testND(List<Collection<IFSAState<VAL, LBL>>> multiStates);

	/**
	 * Same as {@link #validStatesND(List)} but considering singleton multi-states.
	 */
	Collection<List<IFSAState<VAL, LBL>>> validStates(List<IFSAState<VAL, LBL>> states);

	/**
	 * Get the lists of simple states from multi-state {@code states} that are valid for the condition, conserving their initial order in {@code states}
	 * 
	 * @param states the multi-state list .
	 * @return the lists of simple state from the multi-state {@code states} that are valid, conserving their initial order in {@code states}.
	 *         A i<sup>th</sup> element of a returned list may be null if no state of the i<sup>th</sup> multi-state from {@code states} is valid for this list.
	 */
	Collection<List<IFSAState<VAL, LBL>>> validStatesND(List<Collection<IFSAState<VAL, LBL>>> states);
}
