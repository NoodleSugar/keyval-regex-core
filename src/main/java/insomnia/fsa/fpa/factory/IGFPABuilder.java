package insomnia.fsa.fpa.factory;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.IGFPA;

/**
 * Builder/Factory of Graph automaton.
 * 
 * @author zuri
 */
public interface IGFPABuilder<VAL, LBL>
{
	/**
	 * Reset the builder to the initial empty state (create an empty automaton).
	 */
	IGFPABuilder<VAL, LBL> clear();

	IGFPABuilder<VAL, LBL> set( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges);

	IGFPABuilder<VAL, LBL> setProperties(IFPAProperties properties);

	IGFPABuilder<VAL, LBL> setRootedStates(Collection<IFSAState<VAL, LBL>> rootedStates);

	IGFPABuilder<VAL, LBL> setTerminalStates(Collection<IFSAState<VAL, LBL>> terminalStates);

	// ========================================================================

	IGFPA<VAL, LBL> create();

	IGFPA<VAL, LBL> create( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFPAProperties properties //
	);
}
