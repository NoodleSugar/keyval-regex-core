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

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdgeCondition;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.implem.fsa.fta.buftachunk.modifier.IBUFTAChunkModifier;
import insomnia.implem.fsa.fta.buftachunk.modifier.IBUFTAChunkModifier.Environment;
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

	private ITree<VAL, LBL> tree;

	private Mode mode;

	private Function<LBL, IFSALabelCondition<LBL>>                           fcreateLabelCondition;
	private Function<VAL, IFSAValueCondition<VAL>>                           fcreateValueCondition;
	private Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> fcreateFTAEdgeCondition;
	private Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> fcreateChildFTAEdgeCondition;

	boolean nodeTerminalConditionOnInitialStates, nodeRootedConditionOnFinalStates;
	boolean internalNodesAreInitial, internalNodesAreFinal;

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

	private void addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, IFTAEdgeCondition<VAL, LBL> condition)
	{
		var parents = Collections.singletonList(state);
		automaton.addFTAEdge(new FTAEdge<>(parents, state, condition));
	}

	private void addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, Function<List<IFSAState<VAL, LBL>>, IFTAEdgeCondition<VAL, LBL>> createCondition)
	{
		var parents = Collections.singletonList(state);
		automaton.addFTAEdge(new FTAEdge<>(parents, state, createCondition.apply(parents)));
	}

	private void addChildFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		addFTAEdge(automaton, state, fcreateChildFTAEdgeCondition);
	}

	private void addFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		addFTAEdge(automaton, state, fcreateFTAEdgeCondition);
	}

	// =========================================================================a

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
			gchunk.setNodeCondition(state, FSANodeConditions.create(isRooted, isTerminal));
		else if (nodeRootedConditionOnFinalStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createRooted(isRooted));
		else if (nodeTerminalConditionOnInitialStates)
			gchunk.setNodeCondition(state, FSANodeConditions.createTerminal(isTerminal));
		else
			gchunk.setNodeCondition(state, FSANodeConditions.createProjection(gchunk, state));
	}

	// =========================================================================a

	private void makeInitialFrom(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state, INode<VAL, LBL> node)
	{
		var                 gchunk = automaton.getGChunk();
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
			finalState = state;
		else
		{
			var newState = gchunk.createState();
			finalState = newState;
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
			makeItInitial(automaton, initialState, node.isTerminal());
			makeItFinal(automaton, finalState, node.isRooted());
		}
		automaton.putOriginalNode(initialState, node);
		automaton.putOriginalNode(finalState, node);
	}

	// =========================================================================

	public BUFTABuilder<VAL, LBL> setChunkModifier(IBUFTAChunkModifier<VAL, LBL> modifier)
	{
		this.modifier = modifier;
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

			if (nodeChilds.size() == 1)
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
						automaton.putOriginalNode(childState, node);
						gchunk.setFinal(childState);
						addChildFTAEdge(automaton, childState);
						gchunk.setNodeCondition(childState, FSANodeConditions.create(false, false));
					}
				}
				List<IFSAState<VAL, LBL>> parents = new ArrayList<>(nodeChildStates);
				automaton.addFTAEdge(new FTAEdge<>(parents, newState, fcreateFTAEdgeCondition.apply(parents)));
			}

			if (internalNodesAreInitial && node != treeRoot)
			{
				gchunk.addEdge(initialLooped, newState, null);
				gchunk.setNodeCondition(newState, FSANodeConditions.create(false, false));
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
		var automaton = buildFromTree(tree);
		applyModifierOn(automaton);
		return new BUFTA<>(automaton);
	}

	// ==========================================================================

	@Override
	public String toString()
	{
		return create().toString();
	}
}
