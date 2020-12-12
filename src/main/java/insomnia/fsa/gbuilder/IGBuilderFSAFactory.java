package insomnia.fsa.gbuilder;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAProperties;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IGFSAFactory;
import insomnia.fsa.IGFSAutomaton;
import insomnia.fsa.algorithm.IFSAAValidation;

/**
 * Factory of {@link AbstractGBuilderFSA}.
 * 
 * @author zuri
 *
 * @param <E> 
 */
public interface IGBuilderFSAFactory<E> extends IGFSAFactory<E>
{
	@Override
	AbstractGBuilderFSA<E> get(Collection<IFSAState<E>> states, //
		Collection<IFSAState<E>> initialStates, //
		Collection<IFSAState<E>> finalStates, //
		Collection<IFSAEdge<E>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<E, IGFSAutomaton<E>> validator //
	);
}
