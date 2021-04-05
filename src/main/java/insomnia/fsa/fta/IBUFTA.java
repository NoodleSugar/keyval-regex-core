package insomnia.fsa.fta;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import insomnia.data.INode;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.fsa.fta.edge.FTAEdge;

/**
 * Bottom-up tree Automaton.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public interface IBUFTA<VAL, LBL> extends IFTA<VAL, LBL>
{
	IGFPA<VAL, LBL> getGFPA();

	/**
	 * Get all {@link IFTAEdge} that may validate parentStates.
	 * A naive implementation may return all the edges of the automaton.
	 * 
	 * @param parentStates the list of multi-States
	 */
	Collection<IFTAEdge<VAL, LBL>> getHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates);

	/**
	 * Get all {@link IFTAEdgeCondition} that may validate a unique multi-state.
	 * A naive implementation may return all edges with one parent of the automaton.
	 * 
	 * @param parentStates the multi-state child states
	 * @return {@link FTAEdge}s which have a parent in a multi-state from {@code parentStates}
	 */
	Collection<IFTAEdge<VAL, LBL>> getOneHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates);

	// =========================================================================

	public static <VAL, LBL> Predicate<IFSAState<VAL, LBL>> statePredicate(IGFPA<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		VAL value = node.getValue();
		return s -> //
		IGFPA.testValue(s.getValueCondition(), value) //
			&& (!automaton.isTerminal(s) || node.isTerminal()) //
			&& (!automaton.isRooted(s) || node.isRooted()) //
		;
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getInitials(IGFPA<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		return IGFPA.getValidStates(automaton, automaton.getInitialStates(), statePredicate(automaton, node));
	}
}