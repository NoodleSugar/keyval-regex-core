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
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.fpa.creational.FPABuilder;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fta.BUFTAMatchers;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;

// TODO: change the name
/**
 * Builder of a BUFTA.
 * 
 * @author zuri
 * @param <VAL> type of node value
 * @param <LBL> type of edge label
 */
public final class BUBuilder<VAL, LBL>
{
	private Collection<IFTAEdge<VAL, LBL>> ftaEdges;
	private GraphChunk<VAL, LBL>           gchunk;

	// =========================================================================

	/**
	 * Create a builder that can build a BUFTA representing a tree.
	 * 
	 * @param tree the tree to represent
	 */
	public BUBuilder(ITree<VAL, LBL> tree)
	{
		this.gchunk   = new GraphChunk<>();
		this.ftaEdges = new ArrayList<>();
		buildFromTree(tree);
	}

	// =========================================================================

	/**
	 * The concrete builded {@link IBUFTA} implementation.
	 * 
	 * @author zuri
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 */
	private static class BUFTA<VAL, LBL> implements IBUFTA<VAL, LBL>
	{
		private IGFPA<VAL, LBL> gfpa;

		private Collection<IFTAEdge<VAL, LBL>> ftaEdges;

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
			// TODO: better approach
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

	// =========================================================================a

	private IFSAState<VAL, LBL> newStateFrom(INode<VAL, LBL> node)
	{
		IFSAState<VAL, LBL> newState = gchunk.createState(node.getValue());
		gchunk.setRooted(newState, node.isRooted());
		gchunk.addState(newState);
		return newState;
	}

	private void buildFromTree(ITree<VAL, LBL> tree)
	{
		Map<INode<VAL, LBL>, IFSAState<VAL, LBL>> stateOf = new HashMap<>();

		ListIterator<INode<VAL, LBL>> nodes = ITree.bottomUpOrder(tree).listIterator();

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
			gchunk.setInitial(newState, true);
			stateOf.put(node, newState);

			if (node.isTerminal())
				gchunk.setTerminal(newState, true);
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
					IFSAState<VAL, LBL> childState  = gchunk.createState();
					gchunk.addEdge(parentState, childState, FSALabelConditions.createEq(edge.getLabel()));
					nodeChildStates.add(childState);
					stateOf.remove(edge.getChild());
				}
				List<IFSAState<VAL, LBL>> parents = new ArrayList<>(nodeChildStates);
				ftaEdges.add(new FTAEdge<>(parents, newState, FTAEdgeConditions.createInclusive(parents)));
			}
		}
		IFSAState<VAL, LBL> root = stateOf.get(tree.getRoot());
		gchunk.setFinal(root, true);

		if (tree.getRoot().isRooted())
			gchunk.setRooted(root, true);
	}

	/**
	 * @return the BUFTA of the initial tree
	 */
	public IBUFTA<VAL, LBL> create()
	{
		return new BUFTA<>(this);
	}

	// ==========================================================================

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
