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
	 * Test a multi-state configuration, where a virtual state is in a multiple states.
	 * Useful for non deterministic automaton.
	 */
	boolean testND(List<Collection<IFSAState<VAL, LBL>>> multiStates);

	Collection<List<IFSAState<VAL, LBL>>> validStates(List<IFSAState<VAL, LBL>> states);

	Collection<List<IFSAState<VAL, LBL>>> validStatesND(List<Collection<IFSAState<VAL, LBL>>> states);
}
