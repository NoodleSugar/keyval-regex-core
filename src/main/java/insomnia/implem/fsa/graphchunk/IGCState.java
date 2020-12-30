package insomnia.implem.fsa.graphchunk;

import java.util.function.Predicate;

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

	@Override
	boolean test(VAL t);
}
