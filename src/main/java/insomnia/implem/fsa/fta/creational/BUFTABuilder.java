package insomnia.implem.fsa.fta.creational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSANodeCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.fsa.fta.IFTAEdgeCondition;
import insomnia.implem.data.Trees;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.implem.fsa.fta.buftachunk.modifier.IBUFTAChunkModifier;
import insomnia.implem.fsa.fta.buftachunk.modifier.IBUFTAChunkModifier.Environment;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.fsa.nodecondition.FSANodeConditions;
import insomnia.implem.fsa.valuecondition.FSAValueConditions;
import insomnia.lib.help.HelpLists;

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

	private ITree<VAL, LBL>      tree;
	private BUFTAChunk<VAL, LBL> automaton, modifiedAutomaton;

	private Mode mode;

	private Function<LBL, IFSALabelCondition<LBL>>                           fcreateLabelCondition;
	private Function<VAL, IFSAValueCondition<VAL>>                           fcreateValueCondition;
	private Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> fcreateFTAEdgeCondition;
	private Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> fcreateChildFTAEdgeCondition;

	private boolean nodeTerminalConditionOnInitialStates, nodeRootedConditionOnFinalStates;
	private boolean internalNodesAreInitial, internalNodesAreFinal;

	private IBUFTAChunkModifier<VAL, LBL> modifier;

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

	BUFTABuilder()
	{
		this(Trees.empty());
	}

	private BUFTABuilder(ITree<VAL, LBL> tree, Mode mode)
	{
		this.tree = tree;
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

	public BUFTABuilder(IBUFTA<VAL, LBL> src)
	{
		setMode(defaultEdgesMode);
		automaton = BUFTAChunk.create(Trees.empty());

		BidiMap<IFSAState<VAL, LBL>, IFSAState<VAL, LBL>> srcToThis = new DualHashBidiMap<>();
		automaton.getGChunk().union(src.getGFPA(), srcToThis);

		for (var ftaEdge : src.getFTAEdges())
		{
			List<IFSAState<VAL, LBL>> parents = CollectionUtils.collect(ftaEdge.getParents(), s -> srcToThis.get(s), new ArrayList<>());
			IFSAState<VAL, LBL>       child   = srcToThis.get(ftaEdge.getChild());
			automaton.addFTAEdge(new FTAEdge<>(parents, child, FTAEdgeConditions.copy(ftaEdge.getCondition(), parents)));
		}
	}

	// =========================================================================

	static <VAL, LBL> BUFTABuilder<VAL, LBL> createClean()
	{
		BUFTABuilder<VAL, LBL> ret = new BUFTABuilder<>();
		ret.automaton = BUFTAChunk.create();
		return ret;
	}

	static public <VAL, LBL> BUFTABuilder<VAL, LBL> getIntersection(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		return new IntersectionBuilder<>(a, b).createBuilder();
	}

	static public <VAL, LBL> BUFTABuilder<VAL, LBL> getIntersection(IBUFTA<VAL, LBL> a, IBUFTA<VAL, LBL> b)
	{
		return new IntersectionBuilder<>(a, b).createBuilder();
	}

	public BUFTABuilder<VAL, LBL> getIntersection(ITree<VAL, LBL> a)
	{
		return new IntersectionBuilder<>(this.create(), a).createBuilder();
	}

	public BUFTABuilder<VAL, LBL> getIntersection(IBUFTA<VAL, LBL> a)
	{
		return new IntersectionBuilder<>(this.create(), a).createBuilder();
	}

	// =========================================================================

	BUFTAChunk<VAL, LBL> getAutomaton()
	{
		if (automaton == null)
			automaton = buildFromTree(tree);

		return automaton;
	}

	BUFTAChunk<VAL, LBL> getModifiedAutomaton()
	{
		if (modifiedAutomaton == null)
		{
			if (null == modifier)
				modifiedAutomaton = getAutomaton();
			else
			{
				modifiedAutomaton = getAutomaton().copyClone();
				applyModifierOn(modifiedAutomaton);
			}
		}
		return modifiedAutomaton;
	}

	// =========================================================================

	private IFSAState<VAL, LBL> newStateFrom(BUFTAChunk<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		IFSAState<VAL, LBL> newState = automaton.getGChunk().createState(fcreateValueCondition.apply(node.getValue()));
		automaton.getGChunk().addState(newState);
		return newState;
	}

	private IFSAState<VAL, LBL> newInitialFrom(BUFTAChunk<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		IFSAState<VAL, LBL> newState = newStateFrom(automaton, node);
		makeInitialFrom(automaton, newState, node);
		return newState;
	}

	// =========================================================================

	private void addAnyLoop(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		automaton.getGChunk().addEdge(state, state, FSALabelConditions.createAnyLoop());
		addFTAEdge(automaton, state, FTAEdgeConditions.createInclusive(state));
	}

	void addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFTAEdge<VAL, LBL> ftaEdge)
	{
		automaton.addFTAEdge(ftaEdge);
	}

	// =========================================================================

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, IFTAEdgeCondition<VAL, LBL> condition)
	{
		return addFTAEdge(automaton, state, state, condition);
	}

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> state, IFTAEdgeCondition<VAL, LBL> condition)
	{
		var parents = Collections.singletonList(state);
		return addFTAEdge(automaton, parents, state, condition);
	}

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> parents, IFSAState<VAL, LBL> state, IFTAEdgeCondition<VAL, LBL> condition)
	{
		var newEdge = new FTAEdge<>(parents, state, condition);
		addFTAEdge(automaton, newEdge);
		return newEdge;
	}

	// =========================================================================

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> createCondition)
	{
		return addFTAEdge(automaton, state, state, createCondition);
	}

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> state, Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> createCondition)
	{
		var parents = Collections.singletonList(parent);
		return addFTAEdge(automaton, parents, state, createCondition);
	}

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> parents, IFSAState<VAL, LBL> state, Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> createCondition)
	{
		return addFTAEdge(automaton, parents, state, createCondition.apply(parents));
	}

	// =========================================================================

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> state)
	{
		return addFTAEdge(automaton, parent, state, fcreateFTAEdgeCondition);
	}

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> parents, IFSAState<VAL, LBL> state)
	{
		return addFTAEdge(automaton, parents, state, fcreateFTAEdgeCondition);
	}

	private FTAEdge<VAL, LBL> addChildFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return addFTAEdge(automaton, state, fcreateChildFTAEdgeCondition);
	}

	private FTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return addFTAEdge(automaton, state, fcreateFTAEdgeCondition);
	}

	// =========================================================================

	private void makeItInitial(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, boolean isTerminal)
	{
		var gchunk = automaton.getGChunk();
		gchunk.setInitial(state);

		if (this.nodeTerminalConditionOnInitialStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createTerminal(isTerminal));
		else
			gchunk.setNodeCondition(state, FSANodeConditions.createProjection(gchunk, state));
	}

	private void makeItFinal(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, boolean isRooted)
	{
		var gchunk = automaton.getGChunk();
		gchunk.setFinal(state);

		if (this.nodeRootedConditionOnFinalStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createRooted(isRooted));
		else
			gchunk.setNodeCondition(state, FSANodeConditions.createProjection(gchunk, state));
	}

	private void makeItInitialFinal(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, boolean isRooted, boolean isTerminal)
	{
		var gchunk = automaton.getGChunk();
		gchunk.setInitial(state);
		gchunk.setFinal(state);

		if (nodeRootedConditionOnFinalStates && nodeTerminalConditionOnInitialStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createEq(isRooted, isTerminal));
		else if (nodeRootedConditionOnFinalStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createRooted(isRooted));
		else if (nodeTerminalConditionOnInitialStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createTerminal(isTerminal));
		else
			gchunk.setNodeCondition(state, FSANodeConditions.createProjection(gchunk, state));
	}

	// =========================================================================

	private void makeInitialFrom(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, INode<VAL, LBL> node)
	{
		var                 gchunk = automaton.getGChunk();
		IFSAState<VAL, LBL> initialState;

		if (node.isTerminal())
		{
			initialState = state;
			gchunk.setTerminal(state);
		}
		else if (FSAValueConditions.isAny(state.getValueCondition()))
		{
			initialState = state;
			addAnyLoop(automaton, state);
		}
		else
		{
			var preNewState = gchunk.createState();
			initialState = preNewState;
			gchunk.addEdge(preNewState, state, null);
			addAnyLoop(automaton, preNewState);
			addFTAEdge(automaton, state, FTAEdgeConditions.createInclusive(state));
		}
		makeItInitial(automaton, initialState, node.isTerminal());
	}

	private void makeItFinalFrom(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, INode<VAL, LBL> node)
	{
		var                 gchunk = automaton.getGChunk();
		IFSAState<VAL, LBL> finalState;

		if (node.isRooted())
		{
			finalState = state;
			gchunk.setRooted(state);
		}
		else if (FSAValueConditions.isAny(state.getValueCondition()))
		{
			finalState = state;
			addAnyLoop(automaton, state);
		}
		else
		{
			var newState = gchunk.createState();
			finalState = newState;
			addAnyLoop(automaton, newState);
			gchunk.addEdge(state, newState, null);
			addFTAEdge(automaton, newState, FTAEdgeConditions.createInclusive(newState));
		}
		makeItFinal(automaton, finalState, node.isRooted());
	}

	private void makeItInitialFinalFrom(BUFTAChunk<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		var                 gchunk     = automaton.getGChunk();
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
			addAnyLoop(automaton, preState);
			addFTAEdge(automaton, state);
		}
		else if (isTerminal)
		{
			var postState = gchunk.createState();
			initialState = state;
			finalState   = postState;

			gchunk.setTerminal(state);

			gchunk.addEdge(state, postState, null);
			addFTAEdge(automaton, postState);
		}
		else
		{
			var preState  = gchunk.createState();
			var postState = gchunk.createState();
			initialState = preState;
			finalState   = postState;

			gchunk.addEdge(preState, state, null);
			gchunk.addEdge(state, postState, null);

			addAnyLoop(automaton, preState);
			addFTAEdge(automaton, postState);
		}
		if (initialState == finalState)
			makeItInitialFinal(automaton, initialState, node.isRooted(), node.isTerminal());
		else
		{
			IFSANodeCondition<VAL, LBL> finalCond;
			makeItInitial(automaton, initialState, node.isTerminal());
			makeItFinal(automaton, finalState, node.isRooted());

			if (nodeRootedConditionOnFinalStates && nodeTerminalConditionOnInitialStates)
				finalCond = FSANodeConditions.createEq(node.isRooted(), node.isTerminal());
			else
				finalCond = FSANodeConditions.createProjection(node.isRooted(), node.isTerminal());

			gchunk.setNodeCondition(finalState, finalCond);
			gchunk.setNodeCondition(initialState, FSANodeConditions.createAny());
		}
		automaton.putOriginalNode(initialState, node);
		automaton.putOriginalNode(finalState, node);
	}

	// =========================================================================

	public BUFTABuilder<VAL, LBL> setChunkModifier(IBUFTAChunkModifier<VAL, LBL> modifier)
	{
		if (Objects.equals(modifier, this.modifier))
			return this;

		modifiedAutomaton = null;
		this.modifier     = modifier;
		return this;
	}

	private void applyModifierOn(BUFTAChunk<VAL, LBL> automaton)
	{
		if (modifier == null)
			return;

		Environment<VAL, LBL> env = new Environment<VAL, LBL>()
		{
			@Override
			public BUFTAChunk<VAL, LBL> build(ITree<VAL, LBL> tree)
			{
				return buildFromTree(tree);
			}
		};
		modifier.accept(automaton, env);
	}

	// =========================================================================

	private BUFTAChunk<VAL, LBL> buildFromTree(ITree<VAL, LBL> tree)
	{
		BUFTAChunk<VAL, LBL> automaton = BUFTAChunk.create(tree);
		GraphChunk<VAL, LBL> gchunk    = automaton.getGChunk();

		if (tree.getNodes().size() == 1)
		{
			makeItInitialFinalFrom(automaton, tree.getRoot());
			return automaton;
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
			var newState = newInitialFrom(automaton, node);
			stateOf.put(node, newState);
			automaton.putOriginalNode(newState, node);
		}

		// Process internal nodes
		while (nodes.hasNext())
		{
			INode<VAL, LBL>       node       = nodes.next();
			IFSAState<VAL, LBL>   newState   = newStateFrom(automaton, node);
			List<IEdge<VAL, LBL>> nodeChilds = tree.getChildren(node);
			automaton.putOriginalNode(newState, node);

			// Optimisation for 1 child node (reuse the same state for the ftaEdfe) ; not available if the node is the final one
			if (nodeChilds.size() == 1 && nodes.hasNext())
			{
				IEdge<VAL, LBL>     edge  = nodeChilds.get(0);
				IFSAState<VAL, LBL> state = stateOf.get(edge.getChild());
				gchunk.addEdge(state, newState, fcreateLabelCondition.apply(edge.getLabel()));
				stateOf.put(node, newState);
				stateOf.remove(edge.getChild());
				addChildFTAEdge(automaton, newState);

				if (internalNodesAreFinal)
				{
					gchunk.setFinal(newState);
					gchunk.setNodeCondition(newState, FSANodeConditions.createEq(false, false));
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
						automaton.putOriginalNode(childState, node);
						gchunk.setFinal(childState);
						addChildFTAEdge(automaton, childState);

						if (node != treeRoot)
							gchunk.setNodeCondition(childState, FSANodeConditions.createEq(false, false));
					}
				}
				List<IFSAState<VAL, LBL>> parents = new ArrayList<>(nodeChildStates);
				addFTAEdge(automaton, parents, newState);
			}

			if (internalNodesAreInitial && node != treeRoot)
			{
				gchunk.addEdge(initialLooped, newState, null);
				gchunk.setNodeCondition(newState, FSANodeConditions.createEq(false, false));
			}
		}
		var rootState = stateOf.get(treeRoot);
		automaton.putOriginalNode(rootState, treeRoot);

		if (internalNodesAreFinal)
			gchunk.setNodeCondition(rootState, FSANodeConditions.createAny());
		else
			makeItFinalFrom(automaton, rootState, treeRoot);

		return automaton;
	}

	public BUFTABuilder<VAL, LBL> setMode(Mode mode)
	{
		if (this.mode == mode)
			return this;

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
		return new BUFTA<>(getModifiedAutomaton());
	}

	/**
	 * Create an automaton of the homomorphisms on the initial tree.
	 * 
	 * @return the BUFTA of the initial tree
	 */
	public IBUFTA<VAL, LBL> createHomomorphic()
	{
		var automaton = getModifiedAutomaton();
		makeItHomomorphic(automaton);
		return new BUFTA<>(automaton);
	}

	// ==========================================================================

	/**
	 * Groups edges by their {@link IFSALabelCondition}
	 */
	private static class EdgeClass<VAL, LBL> implements Iterable<List<IFSAEdge<VAL, LBL>>>
	{
		private IFSALabelCondition<LBL> labelCondition;

		private List<IFSAEdge<VAL, LBL>> edges;

		private Iterable<List<IFSAEdge<VAL, LBL>>> selected;

		EdgeClass(IFSALabelCondition<LBL> labelCondition, Collection<IFSAEdge<VAL, LBL>> edges)
		{
			this.labelCondition = labelCondition;
			this.edges          = new LinkedList<>(edges);
			this.edges.sort((x, y) -> IFSAState.compare(x.getParent(), y.getParent()));
		}

		/**
		 * Iterate trough all the subset of {@link IFSAEdge}s such as an each element of the subset project on another.
		 * Each subset represents {@link IFSAEdge} that may be merge together.
		 */
		@Override
		public Iterator<List<IFSAEdge<VAL, LBL>>> iterator()
		{
			return IteratorUtils.filteredIterator(HelpLists.powerSet(edges), //
				s -> s.size() > 1 && HelpLists.eachMatchAny(s, (x, y) -> IFSAState.hasProjection(x.getParent(), y.getParent())) //
			);
		}
	}

	private List<EdgeClass<VAL, LBL>> classes(Collection<IFSAEdge<VAL, LBL>> edgesTo)
	{
		var stream = HelpLists.disjointClassesAsStream(edgesTo, (x, y) -> {
			return IFSALabelCondition.equals(x.getLabelCondition(), y.getLabelCondition()) //
			;
		}) //
			.filter(e -> e.size() > 1) //
			.map(e -> new EdgeClass<VAL, LBL>(e.get(0).getLabelCondition(), e)) //
		;
		return stream.collect(Collectors.toList());
	}

	private List<List<Collection<IFSAEdge<VAL, LBL>>>> selectChildsEdges(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> childStates)
	{
		List<List<Collection<IFSAEdge<VAL, LBL>>>> ret = new ArrayList<>();

		for (var childState : childStates)
		{
			List<Collection<IFSAEdge<VAL, LBL>>> childEdges = new ArrayList<>();

			for (var ftaEdge : automaton.getFTAEdgesTo(childState))
				childEdges.add(automaton.getGChunk().getEdgesTo(ftaEdge.getParents()));

			ret.add(childEdges);
		}
		return ret;
	}

	private Iterable<List<IFSAState<VAL, LBL>>> getAllChildsEdgesCombinations(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> childStates)
	{
		var childsEdges = selectChildsEdges(automaton, childStates);

		return (Iterable<List<IFSAState<VAL, LBL>>>) () -> HelpLists.cartesianProductAsStream(childsEdges) //
			.map((e) -> {
				return e.stream().flatMap(x -> x.stream()).map(s -> s.getChild()).collect(Collectors.toList());
			}).iterator();
	}

	private class AddInformations
	{
		// All used in hashCode/equals with reflection
		@SuppressWarnings("unused")
		private IFSAState<VAL, LBL>             parent;
		@SuppressWarnings("unused")
		private Collection<IFSAState<VAL, LBL>> childStates;
		@SuppressWarnings("unused")
		private IFSALabelCondition<LBL>         label;

		AddInformations(IFSAState<VAL, LBL> parent, Collection<IFSAState<VAL, LBL>> childStates, IFSALabelCondition<LBL> label)
		{
			this.parent      = parent;
			this.childStates = childStates;
			this.label       = label;
		}

		@Override
		public boolean equals(Object obj)
		{
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public int hashCode()
		{
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	private void makeItHomomorphic(BUFTAChunk<VAL, LBL> automaton)
	{
		Set<AddInformations>      added    = new HashSet<>();
		var                       gchunk   = automaton.getGChunk();
		Queue<IFTAEdge<VAL, LBL>> ftaEdges = CollectionUtils.select( //
			automaton.getFTAEdges(), //
			e -> e.getParents().size() > 1, //
			new LinkedList<>());

		while (!ftaEdges.isEmpty())
		{
			var                       ftaEdge     = ftaEdges.poll();
			var                       edgesTo     = gchunk.getEdgesTo(ftaEdge.getParents());
			List<EdgeClass<VAL, LBL>> edgeClasses = classes(edgesTo);

			for (var edgeClass : HelpLists.cartesianProductIterable(edgeClasses))
			{
				Collection<IFSAState<VAL, LBL>> dropFTAEdgeParents = new ArrayList<>();

				for (var selectedEdges : edgeClass)
				{
					IFSALabelCondition<LBL>   labelCondition  = selectedEdges.get(0).getLabelCondition();
					List<IFSAState<VAL, LBL>> selectedChilds  = CollectionUtils.collect(selectedEdges, e -> e.getParent(), new ArrayList<>(selectedEdges.size()));
					List<IFSAState<VAL, LBL>> selectedParents = CollectionUtils.collect(selectedEdges, e -> e.getChild(), new ArrayList<>(selectedEdges.size()));
					dropFTAEdgeParents.addAll(selectedParents);

					var subState = gchunk.createMergeState(selectedChilds);
					selectedChilds = CollectionUtils.select(selectedChilds, s -> !gchunk.isInitial(s), new ArrayList<>());

					for (List<IFSAState<VAL, LBL>> childStatesCombination : getAllChildsEdgesCombinations(automaton, selectedChilds))
					{
						AddInformations addInfos = new AddInformations(ftaEdge.getChild(), childStatesCombination, labelCondition);

						if (added.contains(addInfos))
							continue;
						added.add(addInfos);

						var parentState = gchunk.copyState(subState);
						var newEdge     = addFTAEdge(automaton, childStatesCombination, parentState);

						var transitionState = gchunk.createState();
						gchunk.addEdge(parentState, transitionState, labelCondition);
						addFTAEdge(automaton, transitionState, ftaEdge.getChild());

						ftaEdges.add(newEdge);
					}
				}
			}
		}
	}

	// ==========================================================================

	@Override
	public String toString()
	{
		return getModifiedAutomaton().toString();
	}
}
