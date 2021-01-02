package insomnia.implem.fsa.gbuilder;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import insomnia.data.ITree;
import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.FSAException;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAutomaton;
import insomnia.fsa.IGFSAutomaton;
import insomnia.fsa.algorithm.GFSAValidation;
import insomnia.implem.fsa.FSAEdgeAny;
import insomnia.implem.fsa.FSAEdgeEpsilon;
import insomnia.implem.fsa.FSAEdgeNumber;
import insomnia.implem.fsa.FSAEdgeRegex;
import insomnia.implem.fsa.FSAEdgeStringEq;
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
	Map<IGCState<VAL>, STATE> buildStates = new HashMap<>();

	Set<IFSAEdge<VAL, LBL>> buildEdges      = new HashSet<>();
	Set<IGCState<VAL>>      processedStates = new HashSet<>();

	Collection<IFSAState<VAL, LBL>> states;
	Collection<IFSAState<VAL, LBL>> finals;
	Collection<IFSAEdge<VAL, LBL>>  edges;

	GraphChunk<VAL, LBL> automaton;
	boolean              mustBeSync;

	Function<IGCState<VAL>, STATE> stateSupplier;

	IGBuilderFSAFactory<VAL, LBL, ITree<VAL, LBL>> builderFSAFactory;

	// =========================================================================

	public GBuilder(GraphChunk<VAL, LBL> gc, Function<IGCState<VAL>, STATE> stateSupplier, IGBuilderFSAFactory<VAL, LBL, ITree<VAL, LBL>> builderFactory)
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

	private STATE makeState(IGCState<VAL> state)
	{
		STATE ret = buildStates.get(state);

		if (null != ret)
			return ret;

		ret = stateSupplier.apply(state);
		buildStates.put(state, ret);
		return ret;
	}

	// =========================================================================
	// Build functions

	private AbstractFSAEdge<VAL, LBL> makeEdge(IGCEdge<LBL> edgeData, IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child) throws FSAException
	{
		AbstractFSAEdge<VAL, LBL> ret;

		if (GCEdges.isEpsilon(edgeData))
		{
			ret = new FSAEdgeEpsilon<VAL, LBL>(parent, child);

			if (automaton.getProperties().isSynchronous())
				throw new FSAException("Synchronous Automaton expected");
		}
		else if (GCEdges.isNumber(edgeData))
			ret = new FSAEdgeNumber<VAL, LBL, Number>(parent, child, (Number) edgeData.getObj());
		else if (GCEdges.isStringEq(edgeData))
			ret = new FSAEdgeStringEq<VAL, LBL>(parent, child, (String) edgeData.getObj());
		else if (GCEdges.isRegex(edgeData))
			ret = new FSAEdgeRegex<VAL, LBL>(parent, child, (String) edgeData.getObj());
		else if (GCEdges.isAny(edgeData))
			ret = new FSAEdgeAny<VAL, LBL>(parent, child);
		else
			throw new InvalidParameterException();

		if (buildEdges.contains(ret))
			return null;

		buildEdges.add(ret);
		return ret;
	}

	public IFSAutomaton<ITree<VAL, LBL>> newBuild() throws FSAException
	{
		STATE initialState = makeState(automaton.getStart());
		STATE finalState   = makeState(automaton.getEnd());

		states = new HashSet<>(automaton.getNbStates());
		finals = new HashSet<>();
		edges  = new ArrayList<>(automaton.getNbEdges());
		states.add(initialState);

		// Avoid a redundant add
		if (finalState != initialState)
			states.add(finalState);

		finals.add(finalState);

		for (IGCState<VAL> state : automaton.getStates())
		{
			if (state.isTerminal())
				finals.add(makeState(state));
		}

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
			states, Collections.singletonList(initialState), finals, //
			edges, //
			automaton.getProperties(), //
			new GFSAValidation<VAL, LBL, ITree<VAL, LBL>>() //
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
