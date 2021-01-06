package insomnia.implem.fsa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import insomnia.fsa.FSAException;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAutomaton;
import insomnia.fsa.IGFSAutomaton;
import insomnia.fsa.algorithm.GFSAValidation;
import insomnia.fsa.edge.FSAEdge;
import insomnia.implem.fsa.graphchunk.GCEdges;
import insomnia.implem.fsa.graphchunk.GraphChunk;
import insomnia.implem.fsa.graphchunk.IGCEdge;
import insomnia.implem.fsa.graphchunk.IGCState;

/**
 * A specific builder for {@link IGFSAutomaton}.
 * This class can build a {@link IGFSAutomaton} given an intermediated representation of the automaton {@link GraphChunk}.
 * 
 * @author zuri
 * @param <V> Value of a node
 * @param <LBL,VAL> Label of edges.
 * @param <STATE> State of GBuilder
 * @param <T> Automaton type returned
 */
public class GBuilder<VAL, LBL, STATE extends GBuilderState<VAL, LBL>>
{
	private Map<IGCState<VAL>, STATE> buildStates = new HashMap<>();

	private Set<IFSAEdge<VAL, LBL>> buildEdges      = new HashSet<>();
	private Set<IGCState<VAL>>      processedStates = new HashSet<>();

	private Collection<IFSAState<VAL, LBL>> rooted;
	private Collection<IFSAState<VAL, LBL>> terminals;
	private Collection<IFSAState<VAL, LBL>> states;
	private Collection<IFSAState<VAL, LBL>> initials;
	private Collection<IFSAState<VAL, LBL>> finals;
	private Collection<IFSAEdge<VAL, LBL>>  edges;

	private GraphChunk<VAL, LBL> automaton;
	private boolean              mustBeSync;

	private Function<IGCState<VAL>, STATE> stateSupplier;

	private IGBuilderFSAFactory<VAL, LBL> builderFSAFactory;

	// =========================================================================

	public GBuilder(GraphChunk<VAL, LBL> gc, Function<IGCState<VAL>, STATE> stateSupplier, IGBuilderFSAFactory<VAL, LBL> builderFactory)
	{
		automaton = gc;

		this.stateSupplier     = stateSupplier;
		this.builderFSAFactory = builderFactory;
	}

	// =========================================================================

	public GBuilder<VAL, LBL, STATE> mustBeSync(boolean mustBeSync)
	{
		this.mustBeSync = mustBeSync;
		return this;
	}

	private void setRooted(STATE state)
	{
		rooted.add(state);
	}

	private void setTerminal(STATE state)
	{
		terminals.add(state);
	}

	private void setInitial(STATE state)
	{
		initials.add(state);
	}

	private void setFinal(STATE state)
	{
		finals.add(state);
	}

	private STATE makeState(IGCState<VAL> state)
	{
		STATE ret = buildStates.get(state);

		if (null != ret)
			return ret;

		ret = stateSupplier.apply(state);
		buildStates.put(state, ret);
		states.add(ret);

		if (state.isRooted())
			setRooted(ret);
		if (state.isTerminal())
			setTerminal(ret);
		if (state.isInitial())
			setInitial(ret);
		if (state.isFinal())
			setFinal(ret);
		return ret;
	}

	// =========================================================================
	// Build functions

	private FSAEdge<VAL, LBL> makeEdge(IGCEdge<LBL> edgeData, IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child) throws FSAException
	{
		FSAEdge<VAL, LBL> ret;
		ret = new FSAEdge<VAL, LBL>(parent, child, edgeData.getLabelCondition());

		if (buildEdges.contains(ret))
			return null;

		buildEdges.add(ret);
		return ret;
	}

	public IFSAutomaton<VAL, LBL> newBuild() throws FSAException
	{
		states    = new HashSet<>(automaton.getNbStates());
		initials  = new HashSet<>();
		finals    = new HashSet<>();
		rooted    = new HashSet<>();
		terminals = new HashSet<>();

		STATE initialState = makeState(automaton.getStart());

		edges = new ArrayList<>(automaton.getNbEdges());

		if (automaton.getProperties().isSynchronous())
			build(automaton.getStart(), initialState);
		else if (mustBeSync)
			buildSync(automaton.getStart(), initialState);
		else
			build(automaton.getStart(), initialState);

// Reindex states
//			int i = 0;
//			for(IFSAState<LBL,VAL> s : states)
//				((State<LBL,VAL>)s).id = i++;

		return builderFSAFactory.get( //
			states, rooted, terminals, initials, finals, //
			edges, //
			automaton.getProperties(), //
			new GFSAValidation<VAL, LBL>() //
		);
	}

	private STATE build_addState(IGCState<VAL> fcurrent) throws FSAException
	{
		STATE newAState;

		if (processedStates.contains(fcurrent))
			newAState = buildStates.get(fcurrent);
		else
		{
			newAState = makeState(fcurrent);
			states.add(newAState);
			build(fcurrent, newAState);
		}
		return newAState;
	}

	private void build_addEdge(IGCEdge<LBL> edgeData, STATE acurrentState, STATE newAState) throws FSAException
	{
		IFSAEdge<VAL, LBL> newAEdge = makeEdge(edgeData, acurrentState, newAState);

		if (null == newAEdge)
			return;

		edges.add(newAEdge);
		acurrentState.childs.add(newAEdge);
	}

	private void build(IGCState<VAL> fstate, STATE acurrentState) throws FSAException
	{
		processedStates.add(fstate);
		Collection<IGCEdge<LBL>> fedges = automaton.getEdges(fstate);
		acurrentState.childs = new ArrayList<>(fedges.size());

		for (IGCEdge<LBL> edgeData : fedges)
		{
			IGCState<VAL> fcurrent  = automaton.edge_getEnd(edgeData);
			STATE         newAState = build_addState(fcurrent);
			build_addEdge(edgeData, acurrentState, newAState);
		}
	}

	private STATE build_addStateSync(IGCState<VAL> fcurrent) throws FSAException
	{
		STATE newAState;

		if (processedStates.contains(fcurrent))
			newAState = buildStates.get(fcurrent);
		else
		{
			newAState = makeState(fcurrent);
			states.add(newAState);
			buildSync(fcurrent, newAState);
		}
		return newAState;
	}

	private void buildSync(IGCState<VAL> fstate, STATE acurrentState) throws FSAException
	{
		processedStates.add(fstate);
		Collection<IGCEdge<LBL>> fedges = automaton.getEdges(fstate);
		acurrentState.childs = new ArrayList<>(fedges.size());

		for (IGCEdge<LBL> edgeData : fedges)
		{
			if (GCEdges.isEpsilon(edgeData))
			{
				Collection<IGCState<VAL>> fstates = automaton.epsilonClosure(fstate);
				Collection<STATE>         astates = fstates.stream().map(s -> makeState(s)).collect(Collectors.toList());

				if (!Collections.disjoint(astates, finals))
					finals.add(makeState(fstate));

				Collection<IGCEdge<LBL>> edges = automaton.getEdges(fstates);

				for (IGCEdge<LBL> edge : edges)
				{
					/*
					 * Skip epsilon edges (not needed)
					 */
					if (GCEdges.isEpsilon(edge))
						continue;

					IGCState<VAL> ftarget   = automaton.edge_getEnd(edge);
					STATE         newAState = build_addStateSync(ftarget);
					build_addEdge(edge, acurrentState, newAState);
				}
			}
			else
			{
				IGCState<VAL> fcurrent  = automaton.edge_getEnd(edgeData);
				STATE         newAState = build_addStateSync(fcurrent);
				build_addEdge(edgeData, acurrentState, newAState);
			}
		}
	}

	// =========================================================================

	@Override
	public String toString()
	{
		return automaton.toString();
	}
}
