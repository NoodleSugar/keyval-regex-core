package insomnia.implem.fsa.fta.creational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.BUFTAOp;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.edge.FSAEdge;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.fsa.state.FSAState;
import insomnia.implem.fsa.valuecondition.FSAValueConditions;

public final class BUBuilder<VAL, LBL>
{
	BUFTA<VAL, LBL> fta;

	// =========================================================================

	public BUBuilder(ITree<VAL, LBL> tree)
	{
		this.fta = buildFromTree(tree);
	}

	// =========================================================================

	private static class BUFTA<VAL, LBL> implements IBUFTA<VAL, LBL>
	{
		Collection<IFSAState<VAL, LBL>> initials, finals, rooted, terminals;

		Map<IFSAState<VAL, LBL>, Collection<IFSAEdge<VAL, LBL>>> edgesOf;

		Collection<IFTAEdge<VAL, LBL>> ftaEdges;

		BUFTA()
		{
			initials  = new HashSet<>();
			finals    = new HashSet<>();
			rooted    = new HashSet<>();
			terminals = new HashSet<>();
			edgesOf   = new HashMap<>();
			ftaEdges  = new ArrayList<>();
		}

		BUFTA(BUFTA<VAL, LBL> src)
		{
			initials  = new ArrayList<>(src.initials);
			finals    = new ArrayList<>(src.finals);
			rooted    = new ArrayList<>(src.rooted);
			terminals = new ArrayList<>(src.terminals);
			edgesOf   = new HashMap<>(src.edgesOf);
			ftaEdges  = new ArrayList<>(src.ftaEdges);
		}

		@Override
		public boolean test(ITree<VAL, LBL> tree)
		{
			return BUFTAOp.test(this, tree);
		}

		@Override
		public Collection<IFSAState<VAL, LBL>> getInitialStates()
		{
			return initials;
		}

		@Override
		public Collection<IFSAState<VAL, LBL>> getFinalStates()
		{
			return finals;
		}

		@Override
		public boolean isRooted(IFSAState<VAL, LBL> state)
		{
			return rooted.contains(state);
		}

		@Override
		public boolean isTerminal(IFSAState<VAL, LBL> state)
		{
			return terminals.contains(state);
		}

		@Override
		public Collection<IFTAEdge<VAL, LBL>> getHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates)
		{
			return ftaEdges;
		}

		@Override
		public Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states)
		{
			Collection<IFSAEdge<VAL, LBL>> ret = new ArrayList<IFSAEdge<VAL, LBL>>();

			for (IFSAState<VAL, LBL> state : states)
				ret.addAll(edgesOf.getOrDefault(state, Collections.emptyList()));

			return ret;
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			sb.append("Initials: ").append(initials).append("\n");
			sb.append("Finals: ").append(finals).append("\n");
			sb.append("Rooted: ").append(rooted).append("\n");
			sb.append("Terminals: ").append(terminals).append("\n");

			sb.append("FSAEdges:\n");
			edgesOf.values().stream().flatMap(c -> c.stream()).forEach(e -> sb.append(e).append("\n"));

			sb.append("FTAEdges:\n");
			ftaEdges.stream().forEach(e -> sb.append(e).append("\n"));

			return sb.toString();
		}
	}

	// =========================================================================

	private BUFTA<VAL, LBL> buildFromTree(ITree<VAL, LBL> tree)
	{
		BUFTA<VAL, LBL> ret      = new BUFTA<>();
		INode<VAL, LBL> treeRoot = tree.getRoot();

		IFSAState<VAL, LBL> ftaRoot = new FSAState<>(FSAValueConditions.createAnyOrEq(tree.getRoot().getValue()));
		ret.finals.add(ftaRoot);

		buildFromTree_recursive(ret, ftaRoot, tree, treeRoot);
		return ret;
	}

	private void addEdge( //
		BUFTA<VAL, LBL> bufta, IFSAState<VAL, LBL> buState, //
		ITree<VAL, LBL> tree, IEdge<VAL, LBL> tEdge)
	{
		IFSAState<VAL, LBL> newState = new FSAState<>(FSAValueConditions.createAnyOrEq(tEdge.getChild().getValue()));
		IFSAEdge<VAL, LBL>  newEdge  = new FSAEdge<>(newState, buState, FSALabelConditions.createEq(tEdge.getLabel()));
		bufta.edgesOf.put(newState, new ArrayList<>(Collections.singleton(newEdge)));
		buildFromTree_recursive(bufta, newState, tree, tEdge.getChild());
	}

	private void buildFromTree_recursive( //
		BUFTA<VAL, LBL> bufta, IFSAState<VAL, LBL> buState, //
		ITree<VAL, LBL> tree, INode<VAL, LBL> tNode)
	{
		if (tNode.isRooted())
			bufta.rooted.add(buState);
		if (tNode.isTerminal())
			bufta.terminals.add(buState);

		Collection<IEdge<VAL, LBL>> tEdges = tree.getChildren(tNode);

		if (tEdges.size() == 0)
		{
			bufta.initials.add(buState);
			return;
		}
		else if (tEdges.size() == 1)
		{
			IEdge<VAL, LBL> tEdge = tEdges.iterator().next();
			addEdge(bufta, buState, tree, tEdge);
			return;
		}
		List<IFSAState<VAL, LBL>> parents = new ArrayList<IFSAState<VAL, LBL>>(tEdges.size());

		for (IEdge<VAL, LBL> tEdge : tree.getChildren(tNode))
		{
			IFSAState<VAL, LBL> silentState = new FSAState<>(FSAValueConditions.createAny());
			addEdge(bufta, silentState, tree, tEdge);
			parents.add(silentState);
		}
		IFTAEdge<VAL, LBL> newEdge = new FTAEdge<>(parents, buState, FTAEdgeConditions.createInclusive(parents));
		bufta.ftaEdges.add(newEdge);
	}

	public IBUFTA<VAL, LBL> build()
	{
		return new BUFTA<>(fta);
	}

	@Override
	public String toString()
	{
		return fta.toString();
	}
}
