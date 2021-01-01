package insomnia.fsa;

import java.util.Collection;
import java.util.List;

/**
 * Classic graph automaton representation.
 * 
 * @param <LBL> Type of the labels.
 * @param <ELMNT> Type of the tested element.
 */
public interface IGFSAutomaton<VAL, LBL, ELMNT> extends IFSAutomaton<ELMNT>
{
	int nbStates();

	IFSAProperties getProperties();

	List<LBL> getLabelsOf(ELMNT element);

	VAL getValueOf(ELMNT element);

	Collection<IFSAState<VAL, LBL>> getInitialStates();

	Collection<IFSAState<VAL, LBL>> getFinalStates();

	Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state);

	Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, ELMNT element);

	Collection<IFSAState<VAL, LBL>> nextValidStates(IFSAState<VAL, LBL> state, ELMNT element);

	Collection<IFSAState<VAL, LBL>> epsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states);

	Collection<IFSAState<VAL, LBL>> epsilonClosure(IFSAState<VAL, LBL> state);

}
