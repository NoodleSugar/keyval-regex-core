package insomnia.implem.fsa.fta.creational;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.data.Trees;
import insomnia.implem.fsa.fpa.creational.FPABuilder;
import insomnia.implem.fsa.fta.BUFTAMatchers;

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

	private Collection<IFTAEdge<VAL, LBL>> ftaEdges;

	private MultiValuedMap<IFSAState<VAL, LBL>, IFTAEdge<VAL, LBL>> ftaEdgesOf;

	private Map<IFSAState<VAL, LBL>, INode<VAL, LBL>> originalNodes;

	private ITree<VAL, LBL> originalTree;

	BUFTA(BUFTABuilder<VAL, LBL> builder)
	{
		this.gfpa          = new FPABuilder<>(builder.getGchunk()).mustBeSync(false).createNewStates(!true).create();
		this.ftaEdges      = List.copyOf(builder.getFtaEdges());
		this.originalNodes = MapUtils.unmodifiableMap(new HashMap<>(builder.getOriginalNodes()));
		this.originalTree  = Trees.subTree(builder.getTree());
		{
			ArrayListValuedHashMap<IFSAState<VAL, LBL>, IFTAEdge<VAL, LBL>> ftaOf = new ArrayListValuedHashMap<>();

			for (var edge : ftaEdges)
				for (var state : edge.getParents())
					ftaOf.put(state, edge);

			this.ftaEdgesOf = ftaOf;
		}
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
	public Collection<IFTAEdge<VAL, LBL>> getFTAEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates)
	{
		Collection<IFTAEdge<VAL, LBL>> ret = new HashSet<>();

		var states = parentStates.stream().flatMap(c -> c.stream().map(s -> s)).iterator();

		for (IFSAState<VAL, LBL> state : (Iterable<IFSAState<VAL, LBL>>) () -> states)
			ret.addAll(ftaEdgesOf.get(state));

		return ret;
	}

	@Override
	public Map<IFSAState<VAL, LBL>, INode<VAL, LBL>> getOriginalNodes()
	{
		return originalNodes;
	}

	@Override
	public ITree<VAL, LBL> getOriginalTree()
	{
		return originalTree;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(gfpa);
		sb.append("FTAEdges:\n");
		ftaEdges.stream().forEach(e -> sb.append(e).append("\n"));

		if (null != originalTree)
		{
			sb.append("\nOriginal nodes:\n");
			originalNodes.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
			sb.append("Original tree:\n").append(ITree.toString(originalTree));
		}
		return sb.toString();
	}
}