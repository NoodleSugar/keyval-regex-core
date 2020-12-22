package insomnia.fsa;

import java.util.Collection;
import java.util.List;

/**
 * Classic graph automaton representation.
 * 
 * @param <E> Type of the labels.
 * @param <ELMNT> Type of the tested element.
 */
public interface IGFSAutomaton<E, ELMNT> extends IFSAutomaton<ELMNT>
{
	int nbStates();

	IFSAProperties getProperties();

	List<E> getLabelsOf(ELMNT element);

	Collection<IFSAState<E>> getInitialStates();

	Collection<IFSAState<E>> getFinalStates();

	Collection<IFSAEdge<E>> getEdges(Collection<? extends IFSAState<E>> states);

	Collection<IFSAEdge<E>> getEdges(IFSAState<E> state);

	Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, ELMNT element);

	Collection<IFSAState<E>> nextValidStates(IFSAState<E> state, ELMNT element);

	Collection<IFSAState<E>> epsilonClosure(Collection<? extends IFSAState<E>> states);

	Collection<IFSAState<E>> epsilonClosure(IFSAState<E> state);

}
