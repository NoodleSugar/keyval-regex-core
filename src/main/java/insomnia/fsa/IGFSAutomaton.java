package insomnia.fsa;

import java.util.Collection;

/**
 * Classic graph automaton representation.
 * 
 * @param <LBL> Type of the labels.
 * @param <IGFSAElement<VAL,LBL>> Type of the tested element.
 */
public interface IGFSAutomaton<VAL, LBL> extends IFSAutomaton<VAL, LBL>
{
	int nbStates();

	IFSAProperties getProperties();

	boolean isRooted(IFSAState<VAL, LBL> state);

	boolean isTerminal(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> getInitialStates();

	Collection<IFSAState<VAL, LBL>> getFinalStates();

	Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, IFSAElement<VAL, LBL> element);

	Collection<IFSAState<VAL, LBL>> nextValidStates(IFSAState<VAL, LBL> state, IFSAElement<VAL, LBL> element);

	Collection<IFSAState<VAL, LBL>> epsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAState<VAL, LBL>> epsilonClosure(IFSAState<VAL, LBL> state);

}
