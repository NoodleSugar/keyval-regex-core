package insomnia.fsa.fta;

import java.util.Collection;
import java.util.List;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

/**
 * Bottom-up tree Automaton.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public interface IBUFTA<VAL, LBL> extends IFTA<VAL, LBL>
{
	/**
	 * Get the states that can be affected to a leaf.
	 */
	Collection<IFSAState<VAL, LBL>> getInitialStates();

	Collection<IFSAState<VAL, LBL>> getFinalStates();

	boolean isRooted(IFSAState<VAL, LBL> state);

	boolean isTerminal(IFSAState<VAL, LBL> state);

	/**
	 * Get all {@link IFTAEdge} that may validate parentStates.
	 * A naive implementation may return all the edges of the automaton.
	 * 
	 * @param parentStates
	 */
	Collection<IFTAEdge<VAL, LBL>> getHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates);

	Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states);
}
