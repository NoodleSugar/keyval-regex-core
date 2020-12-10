package fsa.gbuilder;

import java.util.Collection;

import fsa.IFSAEdge;
import fsa.IFSAProperties;
import fsa.IFSAState;
import fsa.IGFSAFactory;
import fsa.IGFSAutomaton;
import fsa.algorithm.IFSAAValidation;

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
