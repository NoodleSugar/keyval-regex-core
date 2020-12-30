package insomnia.fsa;

import java.util.Collection;

import insomnia.fsa.algorithm.IFSAAValidation;

/**
 * Factory of Graph automaton.
 * 
 * @author zuri
 * @param <E>
 */
public interface IGFSAFactory<VAL, LBL, ELMNT>
{
	IGFSAutomaton<VAL, LBL, ELMNT> get( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<ELMNT, IGFSAutomaton<VAL, LBL, ELMNT>> validator //
	);
}
