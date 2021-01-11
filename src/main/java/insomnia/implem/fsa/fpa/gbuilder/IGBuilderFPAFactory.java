package insomnia.implem.fsa.fpa.gbuilder;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.algorithm.IGFPAValidation;
import insomnia.fsa.fpa.factory.IGFPAFactory;

/**
 * Factory of {@link AbstractGBuilderFPA}.
 * 
 * @author zuri
 * @param <E>
 */
public interface IGBuilderFPAFactory<VAL, LBL> extends IGFPAFactory<VAL, LBL>
{
	@Override
	AbstractGBuilderFPA<VAL, LBL> create(Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFPAProperties properties, //
		IGFPAValidation<VAL, LBL> validator //
	);
}
