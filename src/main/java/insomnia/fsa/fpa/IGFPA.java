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
	int nbStates();

	int nbEdges();

	int nbEdges(Collection<? extends IFSAState<VAL, LBL>> states);

	int nbEdges(IFSAState<VAL, LBL> state);

	IFPAProperties getProperties();

	boolean isRooted(IFSAState<VAL, LBL> state);

	boolean isTerminal(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> getStates();

	Collection<IFSAState<VAL, LBL>> getInitialStates();

	Collection<IFSAState<VAL, LBL>> getFinalStates();

	Collection<IFSAEdge<VAL, LBL>> getEdges();

	Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, IFPAPath<VAL, LBL> element);

	Collection<IFSAState<VAL, LBL>> nextValidStates(IFSAState<VAL, LBL> state, IFPAPath<VAL, LBL> element);

	Collection<IFSAState<VAL, LBL>> epsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAState<VAL, LBL>> epsilonClosure(IFSAState<VAL, LBL> state);

	// ========================================================================
	// Help

	boolean isInitial(IFSAState<VAL, LBL> state);

	boolean isFinal(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> getRootedStates();

	Collection<IFSAState<VAL, LBL>> getTerminalStates();
}
