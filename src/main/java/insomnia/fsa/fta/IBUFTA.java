package insomnia.fsa.fta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import insomnia.data.INode;
import insomnia.data.ITree;
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

	/**
	 * Get the original tree node that {@code state} represents.
	 * 
	 * @param state the state
	 * @return the node, or {@code null} if unavailable
	 */
	Map<IFSAState<VAL, LBL>, INode<VAL, LBL>> getOriginalNodes();

	/**
	 * Get the original tree used to build the automaton.
	 * 
	 * @return the tree
	 */
	ITree<VAL, LBL> getOriginalTree();
	// =========================================================================

}