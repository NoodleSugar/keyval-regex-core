package insomnia.implem.fsa.fta.creational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.TreeOp;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.fpa.creational.FPABuilder;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fpa.graphchunk.IGCAFactory;
import insomnia.implem.fsa.fta.BUFTAMatchers;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;

// TODO: change the name
public final class BUBuilder<VAL, LBL>
{
	Collection<IFTAEdge<VAL, LBL>> ftaEdges;
	GraphChunk<VAL, LBL>           gchunk;

	// =========================================================================

	public BUBuilder(ITree<VAL, LBL> tree)
	{
		this.gchunk   = new GraphChunk<>();
		this.ftaEdges = new ArrayList<>();
		buildFromTree(tree);
	}

	// =========================================================================

	private static class BUFTA<VAL, LBL> implements IBUFTA<VAL, LBL>
	{
		IGFPA<VAL, LBL> gfpa;

		Collection<IFTAEdge<VAL, LBL>> ftaEdges;

		BUFTA(BUBuilder<VAL, LBL> builder)
		{
			this.gfpa     = new FPABuilder<>(builder.gchunk).mustBeSync(false).create();
			this.ftaEdges = new ArrayList<>(builder.ftaEdges);
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
		public Collection<IFTAEdge<VAL, LBL>> getHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates)
		{
			//TODO: better approach
			return ftaEdges;
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			sb.append(gfpa);
			sb.append("FTAEdges:\n");
			ftaEdges.stream().forEach(e -> sb.append(e).append("\n"));

			return sb.toString();
		}
	}

	// =========================================================================

	private IFSAState<VAL, LBL> newStateFrom(INode<VAL, LBL> node)
	{
		IGCAFactory<VAL, LBL> afactory = gchunk.getAFactory();
		IFSAState<VAL, LBL>   newState = afactory.create(node.getValue());
		afactory.setRooted(newState, node.isRooted());
		gchunk.addState(newState);
		return newState;
	}

	private void buildFromTree(ITree<VAL, LBL> tree)
	{
		// TODO: encode roote/terminal cases
		Map<INode<VAL, LBL>, IFSAState<VAL, LBL>> stateOf = new HashMap<>();

		IGCAFactory<VAL, LBL>         afactory = gchunk.getAFactory();
		ListIterator<INode<VAL, LBL>> nodes    = TreeOp.bottomUpOrder(tree).listIterator();

		// Process Leaves
		while (nodes.hasNext())
		{
			INode<VAL, LBL> node = nodes.next();

			if (0 < tree.getChildren(node).size())
			{
				nodes.previous();
				break;
			}

			IFSAState<VAL, LBL> newState = newStateFrom(node);
			afactory.setInitial(newState, true);
			stateOf.put(node, newState);

			if (node.isTerminal())
				afactory.setTerminal(newState, true);
		}

		// Process internal nodes
		while (nodes.hasNext())
		{
			INode<VAL, LBL>       node       = nodes.next();
			List<IEdge<VAL, LBL>> nodeChilds = tree.getChildren(node);

			if (nodeChilds.size() == 1)
			{
				IEdge<VAL, LBL>     edge     = nodeChilds.get(0);
				IFSAState<VAL, LBL> newState = newStateFrom(node);
				IFSAState<VAL, LBL> state    = stateOf.get(edge.getChild());
				gchunk.addEdge(state, newState, FSALabelConditions.createEq(edge.getLabel()));
				stateOf.put(node, newState);
				stateOf.remove(edge.getChild());
			}
			else
			{
				IFSAState<VAL, LBL>             newState        = newStateFrom(node);
				Collection<IFSAState<VAL, LBL>> nodeChildStates = new HashSet<>();
				stateOf.put(node, newState);

				for (IEdge<VAL, LBL> edge : nodeChilds)
				{
					IFSAState<VAL, LBL> parentState = stateOf.get(edge.getChild());
					IFSAState<VAL, LBL> childState  = afactory.create();
					gchunk.addEdge(parentState, childState, FSALabelConditions.createEq(edge.getLabel()));
					nodeChildStates.add(childState);
					stateOf.remove(edge.getChild());
				}
				List<IFSAState<VAL, LBL>> parents = new ArrayList<>(nodeChildStates);
				ftaEdges.add(new FTAEdge<>(parents, newState, FTAEdgeConditions.createInclusive(parents)));
			}
		}
		IFSAState<VAL, LBL> root = stateOf.get(tree.getRoot());
		afactory.setFinal(root, true);

		if (tree.getRoot().isRooted())
			afactory.setRooted(root, true);
	}

	private void addEdge( //
		IFSAState<VAL, LBL> buState, //
		ITree<VAL, LBL> tree, IEdge<VAL, LBL> tEdge)
	{
		IGCAFactory<VAL, LBL> afactory = gchunk.getAFactory();
		IFSAState<VAL, LBL>   newState = afactory.create(tEdge.getChild().getValue());
		gchunk.addEdge(newState, buState, FSALabelConditions.createEq(tEdge.getLabel()));
		buildFromTree_recursive(newState, tree, tEdge.getChild());
	}

	private void buildFromTree_recursive( //
		IFSAState<VAL, LBL> buState, //
		ITree<VAL, LBL> tree, INode<VAL, LBL> tNode)
	{
		IGCAFactory<VAL, LBL> afactory = gchunk.getAFactory();

		if (tNode.isRooted())
			afactory.setRooted(buState, true);
		if (tNode.isTerminal())
			afactory.setTerminal(buState, true);

		Collection<IEdge<VAL, LBL>> tEdges = tree.getChildren(tNode);

		if (tEdges.size() == 0)
		{
			afactory.setInitial(buState, true);
			return;
		}
		else if (tEdges.size() == 1)
		{
			IEdge<VAL, LBL> tEdge = tEdges.iterator().next();
			addEdge(buState, tree, tEdge);
			return;
		}
		List<IFSAState<VAL, LBL>> parents = new ArrayList<IFSAState<VAL, LBL>>(tEdges.size());

		for (IEdge<VAL, LBL> tEdge : tree.getChildren(tNode))
		{
			IFSAState<VAL, LBL> silentState = afactory.create();
			addEdge(silentState, tree, tEdge);
			parents.add(silentState);
		}
		IFTAEdge<VAL, LBL> newEdge = new FTAEdge<>(parents, buState, FTAEdgeConditions.createInclusive(parents));
		ftaEdges.add(newEdge);
	}

	public IBUFTA<VAL, LBL> create()
	{
		return new BUFTA<>(this);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(gchunk);
		sb.append("FTAEdges:\n");
		ftaEdges.stream().forEach(e -> sb.append(e).append("\n"));

		return sb.toString();
	}
}
