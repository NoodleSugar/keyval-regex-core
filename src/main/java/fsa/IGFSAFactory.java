package insomnia.FSA;

import java.util.Collection;

import insomnia.FSA.algorithm.IFSAAValidation;

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
