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
public interface IGBuilderFSAFactory<VAL, LBL, ELMNT> extends IGFSAFactory<VAL, LBL, ELMNT>
{
	@Override
	AbstractGBuilderFSA<VAL, LBL, ELMNT> get(Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<ELMNT, IGFSAutomaton<VAL, LBL, ELMNT>> validator //
	);
}
