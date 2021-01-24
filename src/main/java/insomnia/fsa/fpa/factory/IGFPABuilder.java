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
public interface IGFPABuilder<VAL, LBL> extends IGFPA<VAL, LBL>
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

	Collection<IFSAEdge<VAL, LBL>> getEdges();

	Collection<IFSAState<VAL, LBL>> getStates();

	Collection<IFSAState<VAL, LBL>> getRootedStates();

	Collection<IFSAState<VAL, LBL>> getTerminalStates();
}
