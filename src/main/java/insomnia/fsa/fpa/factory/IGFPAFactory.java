package insomnia.fsa.fpa.factory;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fpa.algorithm.IGFPAValidation;

/**
 * Factory of Graph automaton.
 * 
 * @author zuri
 * @param <E>
 */
public interface IGFPAFactory<VAL, LBL>
{
	IGFPA<VAL, LBL> create( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFPAProperties properties, //
		IGFPAValidation<VAL, LBL> validation);
}
