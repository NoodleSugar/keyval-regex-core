package insomnia.implem.fsa.gbuilder;

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
 * @param <E>
 */
public interface IGBuilderFSAFactory<E, ELMNT> extends IGFSAFactory<E, ELMNT>
{
	@Override
	AbstractGBuilderFSA<E, ELMNT> get(Collection<IFSAState<E>> states, //
		Collection<IFSAState<E>> initialStates, //
		Collection<IFSAState<E>> finalStates, //
		Collection<IFSAEdge<E>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<ELMNT, IGFSAutomaton<E, ELMNT>> validator //
	);
}
