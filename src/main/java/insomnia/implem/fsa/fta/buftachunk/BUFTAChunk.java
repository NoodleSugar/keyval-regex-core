package insomnia.implem.fsa.fta.buftachunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.data.Trees;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fta.edge.FTAEdge;

public final class BUFTAChunk<VAL, LBL> implements IBUFTA<VAL, LBL>
{
	/**
	 * Original nodes of the tree used to construct the automaton each associated to its represented state.
	 */
	private Map<IFSAState<VAL, LBL>, INode<VAL, LBL>>            stateNodeMap;
	private MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap;

	private Collection<IFTAEdge<VAL, LBL>> ftaEdges;
	private Set<IFSAState<VAL, LBL>>       ftaChilds;
	private GraphChunk<VAL, LBL>           gChunk;
	private ITree<VAL, LBL>                tree;

	private IFSAState<VAL, LBL>       root;
	private List<IFSAState<VAL, LBL>> leaves;

	// =========================================================================

	private BUFTAChunk(ITree<VAL, LBL> tree)
	{
		this.tree          = tree;
		this.gChunk        = new GraphChunk<>();
		this.ftaEdges      = new HashSet<>();
		this.ftaChilds     = new HashSet<>();
		this.stateNodeMap  = new HashMap<>();
		this.nodeStatesMap = new HashSetValuedHashMap<>();
	}

	public static <VAL, LBL> BUFTAChunk<VAL, LBL> create()
	{
		return new BUFTAChunk<>(Trees.empty());
	}

	public static <VAL, LBL> BUFTAChunk<VAL, LBL> create(ITree<VAL, LBL> tree)
	{
		return new BUFTAChunk<>(tree);
	}

	public static <VAL, LBL> BUFTAChunk<VAL, LBL> createOneEdge(boolean isRooted, IFSALabelCondition<LBL> labelCondition, VAL aval, VAL bval)
	{
		BUFTAChunk<VAL, LBL> ret = create();
		ret.gChunk = GraphChunk.createOneEdge(isRooted, labelCondition, bval, aval);
		ret.root   = ret.gChunk.getEnd();
		ret.leaves = Collections.singletonList(ret.gChunk.getStart());
		return ret;
	}

	public static <VAL, LBL> BUFTAChunk<VAL, LBL> createOneState(boolean isRooted, boolean isTerminal, VAL value)
	{
		BUFTAChunk<VAL, LBL> ret = create();
		ret.gChunk = GraphChunk.createOneState(isRooted, isTerminal, value);
		ret.root   = ret.gChunk.getStart();
		ret.leaves = Collections.singletonList(ret.root);
		return ret;
	}

	// =========================================================================

	public IFSAState<VAL, LBL> getRoot()
	{
		return root;
	}

	public List<IFSAState<VAL, LBL>> getLeaves()
	{
		return leaves;
	}

	public IFSAState<VAL, LBL> getLeaf()
	{
		return CollectionUtils.extractSingleton(leaves);
	}

	public void setRoot(IFSAState<VAL, LBL> root)
	{
		this.root = root;
	}

	public void setLeaves(List<IFSAState<VAL, LBL>> leaves)
	{
		this.leaves = new ArrayList<>(leaves);
	}

	public void setLeaf(IFSAState<VAL, LBL> leaf)
	{
		this.leaves = Collections.singletonList(leaf);
	}

	public void cleanGraph()
	{
		gChunk.cleanGraph();
		ftaEdges.clear();
		ftaChilds.clear();
		stateNodeMap.clear();
		nodeStatesMap.clear();
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
		ret.ftaEdges      = new HashSet<>(ftaEdges);
		ret.ftaChilds     = new HashSet<>(ftaChilds);
		ret.stateNodeMap  = new HashMap<>(stateNodeMap);
		ret.nodeStatesMap = new HashSetValuedHashMap<>(nodeStatesMap);
		return ret;
	}

	public BUFTAChunk<VAL, LBL> copy()
	{
		BUFTAChunk<VAL, LBL> ret = BUFTAChunk.create();

		Map<IFSAState<VAL, LBL>, IFSAState<VAL, LBL>> oldToNew = new HashMap<>();

		ret.gChunk = this.gChunk.copy(oldToNew);
		ret.root   = oldToNew.get(this.root);
		ret.leaves = this.leaves.stream().map(oldToNew::get).collect(Collectors.toList());

		for (var fedge : this.getFTAEdges())
		{
			var parents = fedge.getParents().stream().map(oldToNew::get).collect(Collectors.toList());
			var child   = oldToNew.get(fedge.getChild());
			ret.addFTAEdge(new FTAEdge<>(parents, child, fedge.getConditionFactory()));
		}

		for (var entry : stateNodeMap.entrySet())
			ret.stateNodeMap.put(oldToNew.get(entry.getKey()), entry.getValue());

		for (var entry : nodeStatesMap.entries())
			ret.nodeStatesMap.put(entry.getKey(), oldToNew.get(entry.getValue()));

		return ret;
	}

	// =========================================================================

	public void union(BUFTAChunk<VAL, LBL> src)
	{
		ftaEdges.addAll(src.getFTAEdges());
		ftaChilds.addAll(src.ftaChilds);
		gChunk.add(src.getGChunk());
		stateNodeMap.putAll(src.stateNodeMap);
		nodeStatesMap.putAll(src.nodeStatesMap);
	}

	public void set(BUFTAChunk<VAL, LBL> src)
	{
		cleanGraph();
		union(src);
		setRoot(src.getRoot());
		setLeaves(src.getLeaves());
	}

	// =========================================================================

	public void concat(BUFTAChunk<VAL, LBL> src, int nb)
	{
		assert nb > 0;
		this.concat(src);
		nb--;

		while (nb-- != 0)
		{
			src = src.copy();
			this.concat(this);
		}
	}

	/**
	 * Concatenate src to this (this.src)
	 * 
	 * @param src the chunk to concatenate
	 */
	public void concat(BUFTAChunk<VAL, LBL> src)
	{
		this.addFTAEdge(src.getFTAEdges());
		this.getGChunk().union(src.getGChunk(), null, null);

		for (var leaf : this.getLeaves())
		{
			this.getGChunk().addEdge(src.getRoot(), leaf, null);
			// The previous node cannot be a terminal one with a concatenation
			this.getGChunk().setTerminal(leaf, false);
		}
		this.setLeaves(src.getLeaves());
	}

	/**
	 * Reversed concatenation (src.this)
	 * 
	 * @param src the chunk to do the concatenation
	 */
	public void concatr(BUFTAChunk<VAL, LBL> src)
	{
		this.addFTAEdge(src.getFTAEdges());
		this.getGChunk().union(src.getGChunk(), null, null);

		for (var leaf : src.getLeaves())
		{
			this.getGChunk().addEdge(this.getRoot(), leaf, null);
			// The previous node cannot be a terminal one with a concatenation
			this.getGChunk().setTerminal(leaf, false);
		}
		this.setRoot(src.getRoot());
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

	@Override
	public boolean contains(IFTAEdge<VAL, LBL> ftaEdge)
	{
		return ftaEdges.contains(ftaEdge);
	}

	@Override
	public boolean isFTAChild(IFSAState<VAL, LBL> state)
	{
		return ftaChilds.contains(state);
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

	public void addFTAEdge(Collection<IFTAEdge<VAL, LBL>> ftaedges)
	{
		ftaEdges.addAll(ftaedges);

		for (var e : ftaedges)
			ftaChilds.add(e.getChild());
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
	public Collection<IFTAEdge<VAL, LBL>> getFTAEdgesTo(Collection<IFSAState<VAL, LBL>> states)
	{
		return IBUFTA.getFTAEdgesTo(this, states);
	}

	@Override
	public Collection<IFTAEdge<VAL, LBL>> getFTAEdges(List<? extends Collection<IFSAState<VAL, LBL>>> parentStates)
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
		return new StringBuilder()//
			.append("root: ").append(getRoot()).append("\n") //
			.append("leaves: ").append(getLeaves()).append("\n") //
			.append(IBUFTA.toString(this)).toString();
	}
}
