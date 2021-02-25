package insomnia.fsa.fpa;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

/**
 * Classic graph automaton representation.
 * 
 * @param <LBL> Type of the labels.
 * @param <IGFSAElement<VAL,LBL>> Type of the tested element.
 */
public interface IGFPA<VAL, LBL> extends IFPA<VAL, LBL>
{
	IFPAProperties getProperties();

	// =========================================================================

	boolean isInitial(IFSAState<VAL, LBL> state);

	boolean isFinal(IFSAState<VAL, LBL> state);

	boolean isRooted(IFSAState<VAL, LBL> state);

	boolean isTerminal(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> getStates();

	Collection<IFSAState<VAL, LBL>> getInitialStates();

	Collection<IFSAState<VAL, LBL>> getFinalStates();

	Collection<IFSAState<VAL, LBL>> getRootedStates();

	Collection<IFSAState<VAL, LBL>> getTerminalStates();

	void epsilonClosure(Collection<IFSAState<VAL, LBL>> states);

	Collection<IFSAState<VAL, LBL>> getEpsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAState<VAL, LBL>> getEpsilonClosure(IFSAState<VAL, LBL> state);

	// =========================================================================

	/**
	 * @return all edges excluding epsilon transitions
	 */
	Collection<IFSAEdge<VAL, LBL>> getEdges();

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdges();

	Collection<IFSAEdge<VAL, LBL>> getAllEdges();

	Collection<IFSAEdge<VAL, LBL>> getEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdgesTo(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesTo(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesTo(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdgesOf(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEpsilonEdgesOf(IFSAState<VAL, LBL> state);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getAllEdgesOf(IFSAState<VAL, LBL> state);

	/**
	 * Get the edges reachable from 'states' through epsilon transitions.
	 */
	Collection<IFSAEdge<VAL, LBL>> getReachableEdges(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getReachableEdges(IFSAState<VAL, LBL> state);
}
