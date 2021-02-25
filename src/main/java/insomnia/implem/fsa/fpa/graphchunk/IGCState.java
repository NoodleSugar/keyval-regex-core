package insomnia.implem.fsa.fpa.graphchunk;

import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;

/**
 * Represent a state of a {@link GraphChunk}.
 * A state may be a test on a value type.
 * 
 * @author zuri
 */
interface IGCState<VAL, LBL> extends IFSAState<VAL, LBL>
{
	boolean isInitial();

	/*
	 * Is the state a final state of the represented automaton ?
	 */
	boolean isFinal();

	boolean isRooted();

	boolean isTerminal();

	IFSAValueCondition<VAL> getValueCondition();
}
