package insomnia.fsa.fta;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;

/**
 * Bottom-up tree Automaton.
 * 
 * @author zuri
 * @param <VAL> type of node value
 * @param <LBL> type of edge label
 */
public interface IBUFTA<VAL, LBL> extends IFTA<VAL, LBL>
{
	IGFPA<VAL, LBL> getGFPA();

	/**
	 * Get all the {@link IFTAEdge} of the automaton.
	 * 
	 * @return a {@link Collection} of the edges.
	 */
	Collection<IFTAEdge<VAL, LBL>> getFTAEdges();

	/**
	 * Get all {@link IFTAEdge} that may validate parentStates.
	 * A naive implementation may return all the edges of the automaton.
	 * 
	 * @param parentStates the list of multi-states
	 */
	Collection<IFTAEdge<VAL, LBL>> getFTAEdges(List<? extends Collection<IFSAState<VAL, LBL>>> parentStates);

	/**
	 * Get all {@link IFTAEdge} having a specific state as child.
	 * 
	 * @param state the child state
	 * @return a {@link Collection} of edges having {@code state} as child.
	 */
	Collection<IFTAEdge<VAL, LBL>> getFTAEdgesTo(IFSAState<VAL, LBL> state);

	/**
	 * Get the mappings from a state to the node it represents.
	 * 
	 * @return a {@link Map} of such mappings.
	 */
	Map<IFSAState<VAL, LBL>, INode<VAL, LBL>> getStateNodeMap();

	/**
	 * Get the mappings from a node to its representative states.
	 * 
	 * @return a {@link Map} of such mappings.
	 */
	Map<INode<VAL, LBL>, Collection<IFSAState<VAL, LBL>>> getNodeStatesMap();

	/**
	 * Get the original node that a state represents.
	 * 
	 * @param state the state
	 * @return the node, or {@code null} if unavailable
	 */
	INode<VAL, LBL> getStateNode(IFSAState<VAL, LBL> state);

	/**
	 * Get all the representative states of a node.
	 * 
	 * @param node the node
	 * @return the {@link IFSAState}s representing {@code state}
	 */
	Collection<IFSAState<VAL, LBL>> getNodeStates(INode<VAL, LBL> node);

	/**
	 * Get the original tree used to build the automaton.
	 * 
	 * @return the tree
	 */
	ITree<VAL, LBL> getOriginalTree();

	// =========================================================================

	public static <VAL, LBL> Collection<IFTAEdge<VAL, LBL>> getFTAEdges(IBUFTA<VAL, LBL> automaton, List<? extends Collection<IFSAState<VAL, LBL>>> parentStates)
	{
		Collection<IFTAEdge<VAL, LBL>> ret = new HashSet<>();

		var states = parentStates.stream().flatMap(c -> c.stream().map(s -> s)).iterator();

		for (IFSAState<VAL, LBL> state : (Iterable<IFSAState<VAL, LBL>>) () -> states)
			ret.addAll(CollectionUtils.select(automaton.getFTAEdges(), e -> e.getParents().contains(state)));

		return ret;
	}

	public static <VAL, LBL> Collection<IFTAEdge<VAL, LBL>> getFTAEdgesTo(IBUFTA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return CollectionUtils.select(automaton.getFTAEdges(), e -> e.getChild() == state);
	}

	// =========================================================================

	public static <VAL, LBL> String toString(IBUFTA<VAL, LBL> automaton)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(automaton.getGFPA());
		sb.append("FTAEdges:\n");
		automaton.getFTAEdges().stream().forEach(e -> sb.append(e).append("\n"));

		if (null != automaton.getOriginalTree())
		{
			sb.append("\nOriginal nodes:\n");
			automaton.getStateNodeMap().forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
			sb.append("Original tree:\n").append(ITree.toString(automaton.getOriginalTree()));
		}
		return sb.toString();
	}
}