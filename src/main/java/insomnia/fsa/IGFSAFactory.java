package insomnia.fsa;

import java.util.Collection;

import insomnia.fsa.algorithm.IGFSAValidation;

/**
 * Factory of Graph automaton.
 * 
 * @author zuri
 * @param <E>
 */
public interface IGFSAFactory<VAL, LBL>
{
	IGFSAutomaton<VAL, LBL> get( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFSAProperties properties, //
		IGFSAValidation<VAL, LBL> validation);
}
