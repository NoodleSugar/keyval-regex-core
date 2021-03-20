package insomnia.implem.fsa.fta.creational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.fsa.fta.IFTAEdgeCondition;
import insomnia.implem.fsa.fpa.creational.FPABuilder;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fta.BUFTAMatchers;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.fsa.valuecondition.FSAValueConditions;

// TODO: change the name
/**
 * Builder of a BUFTA.
 * 
 * @author zuri
 * @param <VAL> type of node value
 * @param <LBL> type of edge label
 */
public final class BUFTABuilder<VAL, LBL>
{
	public final static Mode defaultEdgesMode = Mode.PROJECTION;

	private Collection<IFTAEdge<VAL, LBL>> ftaEdges;
	private GraphChunk<VAL, LBL>           gchunk;
	private ITree<VAL, LBL>                tree;

	/**
	 * If {@code true}: the builder must generate an hyper-arc for a one child node of the tree pattern.
	 */
	private boolean checkOneFTAEdges;
	private boolean builded;
	private Mode    mode;

	private Function<LBL, IFSALabelCondition<LBL>>                           fcreateLabelCondition;
	private Function<VAL, IFSAValueCondition<VAL>>                           fcreateValueCondition;
	private Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> fcreateFTAEdgeCondition;

	public enum Mode
	{
		/**
		 * Build an automaton of the structurally equivalent trees of the initial tree
		 */
		STRUCTURE,
		/**
		 * Build an automaton of the projections on the initial tree
		 */
		PROJECTION,
		/**
		 * Build an automaton of the structural projections on the initial tree
		 */
		STRUCTURE_PROJECTION,
		/**
		 * Build an automaton of the trees equals to the initial tree
		 */
		EQUALITY,
	};

	// =========================================================================

	private BUFTABuilder(ITree<VAL, LBL> tree, Mode mode)
	{
		this.tree     = tree;
		this.gchunk   = new GraphChunk<>();
		this.ftaEdges = new ArrayList<>();
		setMode(mode);
	}

	/**
	 * Create a builder that can build a BUFTA representing a tree.
	 * 
	 * @param tree the tree to represent
	 */
	public BUFTABuilder(ITree<VAL, LBL> tree)
	{
		this(tree, defaultEdgesMode);
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

		BUFTA(BUFTABuilder<VAL, LBL> builder)
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
			return ftaEdges.stream().filter(e -> e.getParents().size() > 1).collect(Collectors.toList());
		}

		@Override
		public Collection<IFTAEdge<VAL, LBL>> getOneHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates)
		{
			return ftaEdges.stream() //
				.filter(e -> e.getParents().size() == 1 && IterableUtils.matchesAny(parentStates, p -> p.contains(e.getParents().get(0)))) //
				.collect(Collectors.toList());
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
		IFSAState<VAL, LBL> newState = gchunk.createState(fcreateValueCondition.apply(node.getValue()));
		gchunk.setRooted(newState, node.isRooted());
		gchunk.addState(newState);
		return newState;
	}

	private void buildFromTree(ITree<VAL, LBL> tree)
	{
		if (builded)
			return;

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
				gchunk.addEdge(state, newState, fcreateLabelCondition.apply(edge.getLabel()));
				stateOf.put(node, newState);
				stateOf.remove(edge.getChild());

				if (checkOneFTAEdges)
				{
					// add a special hyperEdge checking one edge condition
					var parents = Collections.singletonList(newState);
					ftaEdges.add(new FTAEdge<>(parents, newState, fcreateFTAEdgeCondition.apply(parents)));
				}
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
					gchunk.addEdge(parentState, childState, fcreateLabelCondition.apply(edge.getLabel()));
					nodeChildStates.add(childState);
					stateOf.remove(edge.getChild());
				}
				List<IFSAState<VAL, LBL>> parents = new ArrayList<>(nodeChildStates);
				ftaEdges.add(new FTAEdge<>(parents, newState, fcreateFTAEdgeCondition.apply(parents)));
			}
		}
		IFSAState<VAL, LBL> root = stateOf.get(tree.getRoot());
		gchunk.setFinal(root, true);

		if (tree.getRoot().isRooted())
			gchunk.setRooted(root, true);

		builded = true;
	}

	public BUFTABuilder<VAL, LBL> setMode(Mode mode)
	{
		if (this.mode == mode)
			return this;

		builded   = false;
		this.mode = mode;

		switch (mode)
		{
		case PROJECTION:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createInclusive;
			fcreateLabelCondition = FSALabelConditions::createAnyOrEq;
			fcreateValueCondition = FSAValueConditions::createAnyOrEq;
			checkOneFTAEdges = false;
			break;
		case STRUCTURE_PROJECTION:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateLabelCondition = FSALabelConditions::createAnyOrEq;
			fcreateValueCondition = FSAValueConditions::createAnyOrEq;
			checkOneFTAEdges = true;
			break;
		case EQUALITY:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateLabelCondition = FSALabelConditions::createEq;
			fcreateValueCondition = FSAValueConditions::createEq;
			checkOneFTAEdges = true;
			break;
		case STRUCTURE:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateLabelCondition = FSALabelConditions::createEq;
			fcreateValueCondition = v -> FSAValueConditions.createAny();
			checkOneFTAEdges = true;
			break;
		default:
			throw new IllegalArgumentException(mode.toString());
		}
		return this;
	}

	/**
	 * @return the BUFTA of the initial tree
	 */
	public IBUFTA<VAL, LBL> create()
	{
		buildFromTree(tree);
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
