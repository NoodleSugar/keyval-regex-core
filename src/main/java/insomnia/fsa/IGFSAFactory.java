package insomnia.fsa;

import java.util.Collection;

import insomnia.fsa.algorithm.IFSAAValidation;

/**
 * Factory of Graph automaton.
 * 
 * @author zuri
 * @param <E>
 */
public interface IGFSAFactory<E, ELMNT>
{
	IGFSAutomaton<E, ELMNT> get( //
		Collection<IFSAState<E>> states, //
		Collection<IFSAState<E>> initialStates, //
		Collection<IFSAState<E>> finalStates, //
		Collection<IFSAEdge<E>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<ELMNT, IGFSAutomaton<E, ELMNT>> validator //
	);
}
