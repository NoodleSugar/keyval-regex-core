package insomnia.implem.fsa.graphchunk;

import insomnia.fsa.IFSAValueCondition;

/**
 * Represent a state of a {@link GraphChunk}.
 * A state may be a test on a value type.
 * 
 * @author zuri
 */
public interface IGCState<VAL>
{
	int getId();

	boolean isInitial();

	/*
	 * Is the state a final state of the represented automaton ?
	 */
	boolean isFinal();

	boolean isRooted();

	boolean isTerminal();

	IFSAValueCondition<VAL> getValueCondition();
}
