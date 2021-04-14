package insomnia.implem.fsa.fta.buftachunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;

public final class BUFTAChunk<VAL, LBL> implements IBUFTA<VAL, LBL>
{
	/**
	 * Original nodes of the tree used to construct the automaton each associated to its represented state.
	 */
	private Map<IFSAState<VAL, LBL>, INode<VAL, LBL>>            stateNodeMap;
	private MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap;

	private Collection<IFTAEdge<VAL, LBL>> ftaEdges;
	private GraphChunk<VAL, LBL>           gChunk;
	private ITree<VAL, LBL>                tree;

	// =========================================================================

	private BUFTAChunk(ITree<VAL, LBL> tree)
	{
		this.tree          = tree;
		this.gChunk        = new GraphChunk<>();
		this.ftaEdges      = new HashSet<>();
		this.stateNodeMap  = new HashMap<>();
		this.nodeStatesMap = new HashSetValuedHashMap<>();

	}

	public static <VAL, LBL> BUFTAChunk<VAL, LBL> create(ITree<VAL, LBL> tree)
	{
		return new BUFTAChunk<>(tree);
	}

	// =========================================================================

	/**
	 * Return a copy of this with the same states and edges objects.
	 */
	public BUFTAChunk<VAL, LBL> copyClone()
	{
		return copyClone(create(tree));
	}

	/**
	 * Copy this into 'ret'.<br>
	 * The copy copy exactly the same {@link IGCState<VAL,LBL>}s and {@link IGCEdge}s.
	 */
	private BUFTAChunk<VAL, LBL> copyClone(BUFTAChunk<VAL, LBL> ret)
	{
		ret.tree          = tree;
		ret.gChunk        = gChunk.copyClone();
		ret.ftaEdges      = new ArrayList<>(ftaEdges);
		ret.stateNodeMap  = new DualHashBidiMap<>(stateNodeMap);
		ret.nodeStatesMap = new HashSetValuedHashMap<>(nodeStatesMap);
		return ret;
	}

	// =========================================================================

	public void union(BUFTAChunk<VAL, LBL> src)
	{
		ftaEdges.addAll(src.getFTAEdges());
		gChunk.union(src.getGChunk());
		stateNodeMap.putAll(src.stateNodeMap);
		nodeStatesMap.putAll(src.nodeStatesMap);
	}

	// =========================================================================

	public ITree<VAL, LBL> getTree()
	{
		return tree;
	}

	public GraphChunk<VAL, LBL> getGChunk()
	{
		return gChunk;
	}

	public Collection<IFTAEdge<VAL, LBL>> getFTAEdges()
	{
		return ftaEdges;
	}

	@Override
	public Map<IFSAState<VAL, LBL>, INode<VAL, LBL>> getStateNodeMap()
	{
		return stateNodeMap;
	}

	@Override
	public Map<INode<VAL, LBL>, Collection<IFSAState<VAL, LBL>>> getNodeStatesMap()
	{
		return nodeStatesMap.asMap();
	}

	@Override
	public INode<VAL, LBL> getStateNode(IFSAState<VAL, LBL> state)
	{
		return stateNodeMap.get(state);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getNodeStates(INode<VAL, LBL> node)
	{
		return nodeStatesMap.get(node);
	}

	// =========================================================================

	public void addFTAEdge(IFTAEdge<VAL, LBL> ftaedge)
	{
		ftaEdges.add(ftaedge);
	}

	public void putOriginalNode(IFSAState<VAL, LBL> state, INode<VAL, LBL> node)
	{
		stateNodeMap.put(state, node);
		nodeStatesMap.put(node, state);
	}

	// =========================================================================

	@Override
	public ITreeMatcher<VAL, LBL> matcher(ITree<VAL, LBL> element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IGFPA<VAL, LBL> getGFPA()
	{
		return getGChunk();
	}

	@Override
	public Collection<IFTAEdge<VAL, LBL>> getFTAEdgesTo(IFSAState<VAL, LBL> state)
	{
		return IBUFTA.getFTAEdgesTo(this, state);
	}

	@Override
	public Collection<IFTAEdge<VAL, LBL>> getFTAEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates)
	{
		return IBUFTA.getFTAEdges(this, parentStates);
	}

	@Override
	public ITree<VAL, LBL> getOriginalTree()
	{
		return tree;
	}

	// =========================================================================

	@Override
	public String toString()
	{
		return IBUFTA.toString(this);
	}
}
