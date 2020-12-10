package fsa;

import java.util.Collection;

import fsa.algorithm.IFSAAValidation;

/**
 * Factory of Graph automaton.
 * 
 * @author zuri
 *
 * @param <E>
 */
public interface IGFSAFactory<E>
{
	IGFSAutomaton<E> get( //
		Collection<IFSAState<E>> states, //
		Collection<IFSAState<E>> initialStates, //
		Collection<IFSAState<E>> finalStates, //
		Collection<IFSAEdge<E>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<E, IGFSAutomaton<E>> validator //
	);
}
