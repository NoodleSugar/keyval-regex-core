package insomnia.fsa;

import java.util.Collection;
import java.util.List;

/**
 * Classic graph automaton representation.
 * 
 * E : type of tested elements
 */
public interface IGFSAutomaton<E> extends IFSAutomaton<E>
{
	int nbStates();

	IFSAProperties getProperties();

	Collection<IFSAState<E>> getInitialStates();

	Collection<IFSAState<E>> getFinalStates();

	Collection<IFSAEdge<E>> getEdges(Collection<? extends IFSAState<E>> states);

	Collection<IFSAEdge<E>> getEdges(IFSAState<E> state);

	Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, List<E> elements);

	Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, E element);

	Collection<IFSAState<E>> nextValidStates(IFSAState<E> state, List<E> elements);

	Collection<IFSAState<E>> nextValidStates(IFSAState<E> state, E element);

	Collection<IFSAState<E>> epsilonClosure(Collection<? extends IFSAState<E>> states);

	Collection<IFSAState<E>> epsilonClosure(IFSAState<E> state);

}
