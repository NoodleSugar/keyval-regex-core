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

	boolean isInitial(IFSAState<VAL, LBL> state);

	boolean isFinal(IFSAState<VAL, LBL> state);

	boolean isRooted(IFSAState<VAL, LBL> state);

	boolean isTerminal(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> getStates();

	Collection<IFSAState<VAL, LBL>> getInitialStates();

	Collection<IFSAState<VAL, LBL>> getFinalStates();

	Collection<IFSAState<VAL, LBL>> getRootedStates();

	Collection<IFSAState<VAL, LBL>> getTerminalStates();

	Collection<IFSAEdge<VAL, LBL>> getEdges();

	Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state);
}
