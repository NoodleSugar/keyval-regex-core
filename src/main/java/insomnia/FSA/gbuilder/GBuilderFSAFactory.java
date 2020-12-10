package insomnia.FSA.gbuilder;

import java.util.Collection;

import insomnia.FSA.IFSAEdge;
import insomnia.FSA.IFSAProperties;
import insomnia.FSA.IFSAState;
import insomnia.FSA.IGFSAFactory;
import insomnia.FSA.IGFSAutomaton;
import insomnia.FSA.algorithm.IFSAAValidation;

public interface GBuilderFSAFactory<E> extends IGFSAFactory<E>
{
	@Override
	GBuilderFSA<E> get(Collection<IFSAState<E>> states, //
		Collection<IFSAState<E>> initialStates, //
		Collection<IFSAState<E>> finalStates, //
		Collection<IFSAEdge<E>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<E, IGFSAutomaton<E>> validator //
	);
}
