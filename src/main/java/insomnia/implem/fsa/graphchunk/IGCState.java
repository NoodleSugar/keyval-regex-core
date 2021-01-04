package insomnia.implem.fsa.graphchunk;

import java.util.function.Predicate;

import insomnia.fsa.IFSAValueCondition;

/**
 * Represent a state of a {@link GraphChunk}.
 * A state may be a test on a value type.
 * 
 * @author zuri
 */
public interface IGCState<VAL> extends Predicate<VAL>
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

	@Override
	boolean test(VAL t);
}
