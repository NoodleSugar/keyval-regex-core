package insomnia.implem.fsa.gbuilder;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAProperties;
import insomnia.fsa.IFSAState;
import insomnia.fsa.algorithm.IGFSAValidation;
import insomnia.fsa.factory.IGFSAFactory;

/**
 * Factory of {@link AbstractGBuilderFSA}.
 * 
 * @author zuri
 * @param <E>
 */
public interface IGBuilderFSAFactory<VAL, LBL> extends IGFSAFactory<VAL, LBL>
{
	@Override
	AbstractGBuilderFSA<VAL, LBL> create(Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFSAProperties properties, //
		IGFSAValidation<VAL, LBL> validator //
	);
}
