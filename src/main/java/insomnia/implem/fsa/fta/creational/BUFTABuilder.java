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
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

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
import insomnia.implem.fsa.nodecondition.FSANodeConditions;
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
	private boolean builded;
	private Mode    mode;

	private Function<LBL, IFSALabelCondition<LBL>>                           fcreateLabelCondition;
	private Function<VAL, IFSAValueCondition<VAL>>                           fcreateValueCondition;
	private Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> fcreateFTAEdgeCondition;
	private Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> fcreateChildFTAEdgeCondition;

	boolean nodeTerminalConditionOnInitialStates, nodeRootedConditionOnFinalStates;
	boolean internalNodesAreInitial, internalNodesAreFinal;

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
		/**
		 * Build an automaton of the semi-twigs of the initial tree
		 */
		SEMI_TWIG,
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

		private MultiValuedMap<IFSAState<VAL, LBL>, IFTAEdge<VAL, LBL>> ftaEdgesOf;

		BUFTA(BUFTABuilder<VAL, LBL> builder)
		{
			this.gfpa     = new FPABuilder<>(builder.gchunk).mustBeSync(false).createNewStates(!true).create();
			this.ftaEdges = List.copyOf(builder.ftaEdges);
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
		public Collection<IFTAEdge<VAL, LBL>> getHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates)
		{
			Collection<IFTAEdge<VAL, LBL>> ret = new HashSet<>();

			var states = parentStates.stream().flatMap(c -> c.stream().map(s -> s)).iterator();

			for (IFSAState<VAL, LBL> state : (Iterable<IFSAState<VAL, LBL>>) () -> states)
				ret.addAll(ftaEdgesOf.get(state));

			return ret;
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
		gchunk.addState(newState);
		return newState;
	}

	private IFSAState<VAL, LBL> newInitialFrom(INode<VAL, LBL> node)
	{
		IFSAState<VAL, LBL> newState = newStateFrom(node);
		makeInitialFrom(newState, node);
		return newState;
	}

	// =========================================================================a

	private void addAnyLoop(IFSAState<VAL, LBL> state)
	{
		gchunk.addEdge(state, state, FSALabelConditions.createAnyLoop());
		addFTAEdge(state, FTAEdgeConditions.createInclusive(state));
	}

	private void addFTAEdge(IFSAState<VAL, LBL> state, IFTAEdgeCondition<VAL, LBL> condition)
	{
		var parents = Collections.singletonList(state);
		ftaEdges.add(new FTAEdge<>(parents, state, condition));
	}

	private void addFTAEdge(IFSAState<VAL, LBL> state, Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> createCondition)
	{
		var parents = Collections.singletonList(state);
		ftaEdges.add(new FTAEdge<>(parents, state, createCondition.apply(parents)));
	}

	private void addChildFTAEdge(IFSAState<VAL, LBL> state)
	{
		addFTAEdge(state, fcreateChildFTAEdgeCondition);
	}

	private void addFTAEdge(IFSAState<VAL, LBL> state)
	{
		addFTAEdge(state, fcreateFTAEdgeCondition);
	}

	// =========================================================================a

	private void makeItInitial(IFSAState<VAL, LBL> state, boolean isTerminal)
	{
		gchunk.setInitial(state);

		if (this.nodeTerminalConditionOnInitialStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createTerminal(isTerminal));
		else
			gchunk.setNodeCondition(state, FSANodeConditions.createProjection(gchunk, state));
	}

	private void makeItFinal(IFSAState<VAL, LBL> state, boolean isRooted)
	{
		gchunk.setFinal(state);

		if (this.nodeRootedConditionOnFinalStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createRooted(isRooted));
		else
			gchunk.setNodeCondition(state, FSANodeConditions.createProjection(gchunk, state));
	}

	private void makeItInitialFinal(IFSAState<VAL, LBL> state, boolean isRooted, boolean isTerminal)
	{
		gchunk.setInitial(state);
		gchunk.setFinal(state);

		if (nodeRootedConditionOnFinalStates && nodeTerminalConditionOnInitialStates)
			gchunk.setNodeCondition(state, FSANodeConditions.create(isRooted, isTerminal));
		else if (nodeRootedConditionOnFinalStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createRooted(isRooted));
		else if (nodeTerminalConditionOnInitialStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createTerminal(isTerminal));
		else
			gchunk.setNodeCondition(state, FSANodeConditions.createProjection(gchunk, state));
	}

	// =========================================================================a

	private void makeInitialFrom(IFSAState<VAL, LBL> state, INode<VAL, LBL> node)
	{
		IFSAState<VAL, LBL> initialState;

		if (node.isTerminal())
		{
			initialState = state;
			gchunk.setTerminal(state);
		}
		else
		{
			if (FSAValueConditions.isAny(state.getValueCondition()))
			{
				initialState = state;
				addAnyLoop(state);
			}
			else
			{
				var preNewState = gchunk.createState();
				initialState = preNewState;
				gchunk.addEdge(preNewState, state, null);
				addAnyLoop(preNewState);
				addFTAEdge(state, FTAEdgeConditions.createInclusive(state));
			}
		}
		makeItInitial(initialState, node.isTerminal());
	}

	private void makeItFinalFrom(IFSAState<VAL, LBL> state, INode<VAL, LBL> node)
	{
		IFSAState<VAL, LBL> finalState;

		if (node.isRooted())
		{
			finalState = state;
			gchunk.setRooted(state);
		}
		else if (FSAValueConditions.isAny(state.getValueCondition()))
			finalState = state;
		else
		{
			var newState = gchunk.createState();
			finalState = newState;
			gchunk.addEdge(state, newState, null);
			addFTAEdge(newState, FTAEdgeConditions.createInclusive(newState));
		}
		makeItFinal(finalState, node.isRooted());
	}

	private void makeItInitialFinalFrom(INode<VAL, LBL> node)
	{
		IFSAState<VAL, LBL> initialState, finalState;
		boolean             isRooted   = node.isRooted();
		boolean             isTerminal = node.isTerminal();

		var state = gchunk.createState(node.getValue());

		if (isRooted && isTerminal)
		{
			gchunk.addState(state);
			gchunk.setRooted(state);
			gchunk.setTerminal(state);
			initialState = state;
			finalState   = state;
		}
		else if (isRooted)
		{
			var preState = gchunk.createState();
			initialState = preState;
			finalState   = state;

			gchunk.setRooted(state);

			gchunk.addEdge(preState, state, null);
			addAnyLoop(preState);
			addFTAEdge(state);
		}
		else if (isTerminal)
		{
			var postState = gchunk.createState();
			initialState = state;
			finalState   = postState;

			gchunk.setTerminal(state);

			gchunk.addEdge(state, postState, null);
			addFTAEdge(postState);
		}
		else
		{
			var preState  = gchunk.createState();
			var postState = gchunk.createState();
			initialState = preState;
			finalState   = postState;

			gchunk.addEdge(preState, state, null);
			gchunk.addEdge(state, postState, null);

			addAnyLoop(preState);
			addFTAEdge(postState);
		}
		if (initialState == finalState)
			makeItInitialFinal(initialState, node.isRooted(), node.isTerminal());
		else
		{
			makeItInitial(initialState, node.isTerminal());
			makeItFinal(finalState, node.isRooted());
		}
	}

	// =========================================================================a

	private void buildFromTree(ITree<VAL, LBL> tree)
	{
		if (builded)
			return;

		if (tree.getNodes().size() == 1)
		{
			makeItInitialFinalFrom(tree.getRoot());
			builded = true;
			return;
		}
		Map<INode<VAL, LBL>, IFSAState<VAL, LBL>> stateOf = new HashMap<>();

		var                           treeRoot      = tree.getRoot();
		ListIterator<INode<VAL, LBL>> nodes         = ITree.bottomUpOrder(tree).listIterator();
		IFSAState<VAL, LBL>           initialLooped = null;

		if (internalNodesAreInitial)
		{
			initialLooped = gchunk.createState();
			gchunk.setInitial(initialLooped);
			gchunk.addEdge(initialLooped, initialLooped, FSALabelConditions.createAnyLoop());
		}

		// Process Leaves
		while (nodes.hasNext())
		{
			INode<VAL, LBL> node = nodes.next();

			if (0 < tree.getChildren(node).size())
			{
				nodes.previous();
				break;
			}
			stateOf.put(node, newInitialFrom(node));
		}

		// Process internal nodes
		while (nodes.hasNext())
		{
			INode<VAL, LBL>       node       = nodes.next();
			IFSAState<VAL, LBL>   newState   = newStateFrom(node);
			List<IEdge<VAL, LBL>> nodeChilds = tree.getChildren(node);

			if (nodeChilds.size() == 1)
			{
				IEdge<VAL, LBL>     edge  = nodeChilds.get(0);
				IFSAState<VAL, LBL> state = stateOf.get(edge.getChild());
				gchunk.addEdge(state, newState, fcreateLabelCondition.apply(edge.getLabel()));
				stateOf.put(node, newState);
				stateOf.remove(edge.getChild());
				addChildFTAEdge(newState);

				if (internalNodesAreFinal)
				{
					gchunk.setFinal(newState);
					gchunk.setNodeCondition(newState, FSANodeConditions.create(false, false));
				}
			}
			else
			{
				Collection<IFSAState<VAL, LBL>> nodeChildStates = new HashSet<>();
				stateOf.put(node, newState);

				for (IEdge<VAL, LBL> edge : nodeChilds)
				{
					IFSAState<VAL, LBL> parentState = stateOf.get(edge.getChild());
					IFSAState<VAL, LBL> childState  = gchunk.createState();
					gchunk.addEdge(parentState, childState, fcreateLabelCondition.apply(edge.getLabel()));
					nodeChildStates.add(childState);
					stateOf.remove(edge.getChild());

					if (internalNodesAreFinal)
					{
						gchunk.setFinal(childState);
						addChildFTAEdge(childState);
						gchunk.setNodeCondition(childState, FSANodeConditions.create(false, false));
					}
				}
				List<IFSAState<VAL, LBL>> parents = new ArrayList<>(nodeChildStates);
				ftaEdges.add(new FTAEdge<>(parents, newState, fcreateFTAEdgeCondition.apply(parents)));
			}

			if (internalNodesAreInitial && node != treeRoot)
			{
				gchunk.addEdge(initialLooped, newState, null);
				gchunk.setNodeCondition(newState, FSANodeConditions.create(false, false));
			}
		}
		var rootState = stateOf.get(treeRoot);

		if (internalNodesAreFinal)
			gchunk.setNodeCondition(rootState, FSANodeConditions.createAny());
		else
			makeItFinalFrom(rootState, treeRoot);

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
			fcreateChildFTAEdgeCondition = FTAEdgeConditions::createInclusive;
			fcreateLabelCondition = FSALabelConditions::createAnyOrEq;
			fcreateValueCondition = FSAValueConditions::createAnyOrEq;
			break;
		case STRUCTURE_PROJECTION:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateChildFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateLabelCondition = FSALabelConditions::createAnyOrEq;
			fcreateValueCondition = v -> FSAValueConditions.createAny();
			break;
		case EQUALITY:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateChildFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateLabelCondition = FSALabelConditions::createEq;
			fcreateValueCondition = FSAValueConditions::createEq;
			nodeTerminalConditionOnInitialStates = true;
			nodeRootedConditionOnFinalStates = true;
			break;
		case STRUCTURE:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateChildFTAEdgeCondition = FTAEdgeConditions::createEq;
			fcreateLabelCondition = FSALabelConditions::createEq;
			fcreateValueCondition = v -> FSAValueConditions.createAny();
			nodeTerminalConditionOnInitialStates = true;
			nodeRootedConditionOnFinalStates = true;
			break;
		case SEMI_TWIG:
			fcreateFTAEdgeCondition = FTAEdgeConditions::createSemiTwig;
			fcreateChildFTAEdgeCondition = FTAEdgeConditions::createInclusive;
			fcreateLabelCondition = FSALabelConditions::createAnyOrEq;
			fcreateValueCondition = FSAValueConditions::createAnyOrEq;
			internalNodesAreInitial = true;

			internalNodesAreFinal = true;
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
