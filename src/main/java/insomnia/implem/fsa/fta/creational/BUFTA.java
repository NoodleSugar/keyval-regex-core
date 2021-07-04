package insomnia.implem.fsa.fta.creational;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.BUFTAProperty;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.data.Trees;
import insomnia.implem.fsa.fpa.creational.FPABuilder;
import insomnia.implem.fsa.fta.BUFTAMatchers;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;

/**
 * The concrete builded {@link IBUFTA} implementation.
 * 
 * @author zuri
 * @param <VAL> type of node value
 * @param <LBL> type of edge label
 */
class BUFTA<VAL, LBL> implements IBUFTA<VAL, LBL>
{
	private IGFPA<VAL, LBL> gfpa;

	private EnumSet<BUFTAProperty> properties;

	private Collection<IFTAEdge<VAL, LBL>> ftaEdges;

	private Collection<IFSAState<VAL, LBL>> ftaChilds;

	private MultiValuedMap<IFSAState<VAL, LBL>, IFTAEdge<VAL, LBL>> ftaEdgesOf;

	private Map<IFSAState<VAL, LBL>, INode<VAL, LBL>> stateNodeMap;

	private MultiValuedMap<INode<VAL, LBL>, IFSAState<VAL, LBL>> nodeStatesMap;

	private ITree<VAL, LBL> originalTree;

	BUFTA(BUFTAChunk<VAL, LBL> automaton)
	{
		this.gfpa         = new FPABuilder<>(automaton.getGChunk()).mustBeSync(false).createNewStates(!true).create();
		this.ftaEdges     = Set.copyOf(automaton.getFTAEdges());
		this.stateNodeMap = MapUtils.unmodifiableMap(new HashMap<>(automaton.getStateNodeMap()));

		ftaChilds = this.ftaEdges.stream().map(e -> e.getChild()).collect(Collectors.toUnmodifiableSet());

		nodeStatesMap = new ArrayListValuedHashMap<>();

		for (var entry : automaton.getNodeStatesMap().entrySet())
			nodeStatesMap.putAll(entry.getKey(), entry.getValue());

		nodeStatesMap = MultiMapUtils.unmodifiableMultiValuedMap(nodeStatesMap);

		this.originalTree = Trees.subTree(automaton.getTree());
		{
			ArrayListValuedHashMap<IFSAState<VAL, LBL>, IFTAEdge<VAL, LBL>> ftaOf = new ArrayListValuedHashMap<>();

			for (var edge : ftaEdges)
				for (var state : edge.getParents())
					ftaOf.put(state, edge);

			this.ftaEdgesOf = ftaOf;
		}
		properties = automaton.getProperties().clone();
	}

	@Override
	public ITreeMatcher<VAL, LBL> matcher(ITree<VAL, LBL> tree)
	{
		return BUFTAMatchers.create(this, tree);
	}

	@Override
	public IGFPA<VAL, LBL> getGFPA()
	{
		return gfpa;
	}

	@Override
	public EnumSet<BUFTAProperty> getProperties()
	{
		return properties.clone();
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

	@Override
	public Collection<IFTAEdge<VAL, LBL>> getFTAEdges()
	{
		return ftaEdges;
	}

	@Override
	public Collection<IFTAEdge<VAL, LBL>> getFTAEdges(List<? extends Collection<IFSAState<VAL, LBL>>> parentStates)
	{
		Collection<IFTAEdge<VAL, LBL>> ret = new HashSet<>();

		var states = parentStates.stream().flatMap(c -> c.stream().map(s -> s)).iterator();

		for (IFSAState<VAL, LBL> state : (Iterable<IFSAState<VAL, LBL>>) () -> states)
			ret.addAll(ftaEdgesOf.get(state));

		return ret;
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

	@Override
	public ITree<VAL, LBL> getOriginalTree()
	{
		return originalTree;
	}

	@Override
	public String toString()
	{
		return IBUFTA.toString(this);
	}
}