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

	/*
	 * Is the state an end state of the represented automaton ?
	 */
	boolean isTerminal();

	IFSAValueCondition<VAL> getValueCondition();

	@Override
	boolean test(VAL t);
}
