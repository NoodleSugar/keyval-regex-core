package insomnia.implem.fsa.fta.creational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import insomnia.data.ITree;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAMultiState;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.fsa.fta.IFTAEdgeCondition;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.fsa.state.FSAMultiStates;
import insomnia.lib.help.HelpIterable;
import insomnia.lib.help.HelpLists;

final class IntersectionBuilder<VAL, LBL>
{
	private IBUFTA<VAL, LBL> a, b;

	IntersectionBuilder(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		this(new BUFTABuilder<>(a).create(),  //
			new BUFTABuilder<>(b).create());
	}

	IntersectionBuilder(IBUFTA<VAL, LBL> a, IBUFTA<VAL, LBL> b)
	{
		this.a = a;
		this.b = b;
	}

	IntersectionBuilder(IBUFTA<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		this(a, new BUFTABuilder<>(b).create());
	}

	// =========================================================================

	private BidiMap<IFSAMultiState<VAL, LBL>, IFSAState<VAL, LBL>> oldToNews;

	private BUFTABuilder<VAL, LBL> build;
	private BUFTAChunk<VAL, LBL>   automaton;
	private GraphChunk<VAL, LBL>   gchunk;

	private boolean processedInitials;

	private final Predicate<IFSAState<VAL, LBL>> truePredicate = s -> true;

	private Set<IFSAMultiState<VAL, LBL>> processed;

	private boolean statesFromTwoAutomatons(Collection<IFSAState<VAL, LBL>> states)
	{
		var astates = a.getGFPA().getStates();
		var bstates = b.getGFPA().getStates();
		return HelpIterable.matchDisjointPredicatesAtLeastOnce(states, List.of(s -> astates.contains(s), s -> bstates.contains(s)));
	}

	private boolean oneAutomatonStuckAtFinal(Collection<IFSAState<VAL, LBL>> states)
	{
		var agfpa = a.getGFPA();
		var bgfpa = b.getGFPA();

		states = CollectionUtils.select(states, s -> (!agfpa.isInitial(s) || agfpa.isRooted(s)) && (!bgfpa.isInitial(s) || bgfpa.isRooted(s)));

		if (states.isEmpty() || statesFromTwoAutomatons(states))
			return false;

		if (IterableUtils.matchesAll(states, s -> (agfpa.isFinal(s) && !agfpa.isRooted(s)) || (bgfpa.isFinal(s) && !bgfpa.isRooted(s))))
			return true;

		return false;
	}

	// =========================================================================

	private IFSAState<VAL, LBL> createMergeState(Collection<IFSAState<VAL, LBL>> states)
	{
		var newState = gchunk.createMergeState(states);

		if (gchunk.isFinal(newState))
		{
			if (!HelpIterable.matchDisjointPredicatesAtLeastOnce(states, List.of(s -> a.getGFPA().isFinal(s), s -> b.getGFPA().isFinal(s))))
				gchunk.setFinal(newState, false);
		}
		if (processedInitials && gchunk.isInitial(newState))
		{
			gchunk.setInitial(newState, false);
		}
		return newState;
	}

	private IFSAMultiState<VAL, LBL> multiStateEClosure(Collection<IFSAState<VAL, LBL>> states)
	{
		return FSAMultiStates.create(CollectionUtils.union( //
			a.getGFPA().getEpsilonClosure(states, truePredicate), //
			b.getGFPA().getEpsilonClosure(states, truePredicate) //
		));
	}

	private IFSAState<VAL, LBL> newFromOld(IFSAMultiState<VAL, LBL> multiState)
	{
		IFSAState<VAL, LBL> newState = oldToNews.get(multiState);

		if (null == newState)
		{
			newState = createMergeState(multiState);
			gchunk.addState(newState);
			oldToNews.put(multiState, newState);
		}
		return newState;
	}

	private Collection<IFSAMultiState<VAL, LBL>> processInitialStates()
	{
		var initialStates = CollectionUtils.union( //
			a.getGFPA().getEpsilonClosure(a.getGFPA().getInitialStates(), truePredicate), //
			b.getGFPA().getEpsilonClosure(b.getGFPA().getInitialStates(), truePredicate) //
		);
		return processStates(initialStates);
	}

	private Collection<IFSAMultiState<VAL, LBL>> processStates(Collection<IFSAState<VAL, LBL>> states)
	{
		Collection<IFSAMultiState<VAL, LBL>> nextStates = new ArrayList<>();
		Set<IFSAMultiState<VAL, LBL>>        classes    = //
			HelpLists.getClasses(states, IFSAState::projectOnMe) //
				.stream().map(FSAMultiStates::create).collect(Collectors.toSet());

		for (IFSAMultiState<VAL, LBL> theClass : classes)
		{
			if (!statesFromTwoAutomatons(theClass))
				continue;

			nextStates.add(theClass);
			newFromOld(theClass);
		}
		return nextStates;
	}

	// =========================================================================

	private void addFPAEdgeClass(Collection<IFSAEdge<VAL, LBL>> edgeClass, IFSAMultiState<VAL, LBL> from, IFSAState<VAL, LBL> newState, IFSALabelCondition<LBL> labelCondition, Collection<IFSAMultiState<VAL, LBL>> nextStates_out)
	{
		IFSAMultiState<VAL, LBL> edgesMultiState = multiStateEClosure(CollectionUtils.collect(edgeClass, IFSAEdge::getChild));

		Collection<IFSAMultiState<VAL, LBL>> stateClasses = processStates(edgesMultiState);

		for (var stateClass : stateClasses)
		{
			if (oneAutomatonStuckAtFinal(stateClass))
				continue;

			var nextState = newFromOld(stateClass);

			// Do not process final non rooted state because following states will be redundant
			if (!gchunk.isFinal(nextState) || gchunk.isRooted(nextState))
				nextStates_out.add(stateClass);

			// Avoid backward edges
			if (CollectionUtils.isSubCollection(stateClass, from))
				continue;

			// Avoid redundancies
			if (IterableUtils.matchesAny( //
				gchunk.getAllEdgesBetween(newState, nextState), //
				e -> FSALabelConditions.projectOn(labelCondition, e.getLabelCondition())) //
			)
				continue;

			gchunk.addEdge(newState, nextState, labelCondition);
		}
	}

	private Collection<IFSAMultiState<VAL, LBL>> processFPAEdges(Collection<IFSAMultiState<VAL, LBL>> currentStates)
	{
		Collection<IFSAMultiState<VAL, LBL>> nextStates = new HashSet<>();

		for (IFSAMultiState<VAL, LBL> multiState : currentStates)
		{
			IFSAState<VAL, LBL> newState = oldToNews.get(multiState);
			var                 allEdges = CollectionUtils.union(a.getGFPA().getEdgesOf(multiState), b.getGFPA().getEdgesOf(multiState));

			Set<Set<IFSAEdge<VAL, LBL>>> classes = HelpLists.getClasses(allEdges, (x, y) -> FSALabelConditions.projectOnMe(x.getLabelCondition(), y.getLabelCondition())) //
				.stream().map(HashSet::new).collect(Collectors.toSet());

			for (var edgeClass : classes)
			{
				IFSALabelCondition<LBL> labelCondition = edgeClass.stream().map(IFSAEdge::getLabelCondition).reduce(FSALabelConditions::intersection).get();
				addFPAEdgeClass(edgeClass, multiState, newState, labelCondition, nextStates);
			}
		}
		processed.addAll(currentStates);
		return nextStates;
	}
	// =========================================================================

	private Collection<IFSAMultiState<VAL, LBL>> processFTAEdges(Collection<IFSAMultiState<VAL, LBL>> currentStates)
	{
		Collection<IFSAMultiState<VAL, LBL>>                              nextStates          = new HashSet<>();
		MultiValuedMap<Set<IFSAMultiState<VAL, LBL>>, IFTAEdge<VAL, LBL>> validMultiStatesMap = new HashSetValuedHashMap<>();

		List<IFSAMultiState<VAL, LBL>> currentStatesList = new ArrayList<>(currentStates);

		int nbStates = currentStatesList.size();
		var ftaEdges = new LinkedList<>(CollectionUtils.union(a.getFTAEdges(currentStatesList), b.getFTAEdges(currentStatesList)));

		for (var ftaEdge : ftaEdges)
		{
			Collection<List<IFSAMultiState<VAL, LBL>>> validNewMultiStates = new ArrayList<>();

			for (List<IFSAState<VAL, LBL>> validMultiState : ftaEdge.getCondition().validStatesND(currentStatesList))
			{
				List<IFSAMultiState<VAL, LBL>> validNewMultiState = new ArrayList<>();

				for (int i = 0; i < nbStates; i++)
				{
					var validState = validMultiState.get(i);

					if (null == validState)
						continue;

					var selected = currentStatesList.get(i);
					validNewMultiState.add(selected);
				}
				validNewMultiStates.add(validNewMultiState);

			}

			for (var validNewMultiState : validNewMultiStates)
				validMultiStatesMap.put(new HashSet<>(validNewMultiState), ftaEdge);
		}
		for (var multiStates : validMultiStatesMap.keySet())
		{
			Collection<IFTAEdge<VAL, LBL>> edges           = validMultiStatesMap.get(multiStates);
			IFSAMultiState<VAL, LBL>       childMultiState = FSAMultiStates.create(CollectionUtils.collect(edges, IFTAEdge::getChild));
			IFTAEdgeCondition<VAL, LBL>    eCondition      = edges.iterator().next().getCondition();
			List<IFSAState<VAL, LBL>>      parents         = CollectionUtils.collect(multiStates, s -> oldToNews.get(s), new ArrayList<>());

			for (var states : processStates(childMultiState))
			{
				var newFTAEdge = new FTAEdge<VAL, LBL>(parents, oldToNews.get(states), FTAEdgeConditions.copy(eCondition, parents));
				build.addFTAEdge(automaton, newFTAEdge);

				if (!processed.contains(states))
					nextStates.add(states);
			}
		}
		return nextStates;
	}

	// =========================================================================

	public BUFTABuilder<VAL, LBL> createBuilder()
	{
		oldToNews = new DualHashBidiMap<>();
		build     = BUFTABuilder.createClean();
		automaton = build.getAutomaton();
		gchunk    = automaton.getGChunk();
		processed = new HashSet<>();

		processedInitials = false;
		Collection<IFSAMultiState<VAL, LBL>> currentStates = processInitialStates();
		Collection<IFSAMultiState<VAL, LBL>> FSAStates;
		processedInitials = true;

		while (!currentStates.isEmpty())
		{
			var edges    = List.copyOf(gchunk.getAllEdges());
			var ftaEdges = List.copyOf(automaton.getFTAEdges());
			FSAStates     = processFPAEdges(currentStates);
			currentStates = processFTAEdges(FSAStates);

			// Nothing added
			if (edges.size() == gchunk.getAllEdges().size() && ftaEdges.size() == automaton.getFTAEdges().size())
				break;
		}

		for (var state : gchunk.getInitialStates())
		{
			if (!gchunk.isTerminal(state))
			{
				gchunk.addEdge(state, state, FSALabelConditions.createAnyLoop());
				automaton.addFTAEdge(new FTAEdge<>(Collections.singletonList(state), state, FTAEdgeConditions.createInclusive(state)));
			}
		}
		for (var state : gchunk.getFinalStates())
		{
			if (!gchunk.isRooted(state))
			{
				gchunk.addEdge(state, state, FSALabelConditions.createAnyLoop());
				automaton.addFTAEdge(new FTAEdge<>(Collections.singletonList(state), state, FTAEdgeConditions.createInclusive(state)));
			}
		}
		return build;
	}
}
