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

import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.FSAException;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAutomaton;
import insomnia.fsa.IGFSAutomaton;
import insomnia.fsa.algorithm.GFSAValidation;
import insomnia.fsa.gbuilder.IGBuilderFSAFactory;
import insomnia.implem.fsa.FSAEdgeEpsilon;
import insomnia.implem.fsa.FSAEdgeNumber;
import insomnia.implem.fsa.FSAEdgeRegex;
import insomnia.implem.fsa.FSAEdgeStringEq;
import insomnia.implem.fsa.gbuilder.GCEdgeData.Type;

/**
 * A specific builder for {@link IGFSAutomaton}.
 * This class can build a {@link IGFSAutomaton} given an intermediated representation of the automaton {@link GraphChunk}.
 * 
 * @author zuri
 * @param <E>
 * @param <STATE>
 */
public class GBuilder<E, STATE extends GBuilderState<E>>
{
	Map<GCState, STATE> buildStates = new HashMap<>();

	Set<IFSAEdge<E>> buildEdges      = new HashSet<>();
	Set<GCState>     processedStates = new HashSet<>();

	Collection<IFSAState<E>> states;
	Collection<IFSAState<E>> finals;
	Collection<IFSAEdge<E>>  edges;

	GraphChunk automaton;
	boolean    mustBeSync;

	Function<Integer, STATE> stateSupplier;
	IGBuilderFSAFactory<E>   builderFactory;

	public GBuilder(GraphChunk gc, Function<Integer, STATE> stateSupplier, IGBuilderFSAFactory<E> builderFactory)
	{
		automaton = gc;

		this.stateSupplier  = stateSupplier;
		this.builderFactory = builderFactory;
	}

	public GBuilder<E, STATE> mustBeSync(boolean mustBeSync)
	{
		this.mustBeSync = mustBeSync;
		return this;
	}

	private STATE makeState(GCState state)
	{
		STATE ret = buildStates.get(state);

		if (null != ret)
			return ret;

		ret = stateSupplier.apply(state.getId());
		buildStates.put(state, ret);
		return ret;
	}

	private AbstractFSAEdge<E> makeEdge(GCEdgeData edgeData, IFSAState<E> parent, IFSAState<E> child) throws FSAException
	{
		AbstractFSAEdge<E> ret;

		switch (edgeData.getType())
		{
		case EPSILON:
			ret = new FSAEdgeEpsilon<E>(parent, child);

			if (automaton.getProperties().isSynchronous())
				throw new FSAException("Synchronous Automaton expected");
			break;
		case NUMBER:
			ret = new FSAEdgeNumber<E, Number>(parent, child, (Number) edgeData.getObj());
			break;
		case STRING_EQUALS:
			ret = new FSAEdgeStringEq<E>(parent, child, (String) edgeData.getObj());
			break;
		case REGEX:
			ret = new FSAEdgeRegex<E>(parent, child, (String) edgeData.getObj());
			break;
		default:
			throw new InvalidParameterException();
		}

		if (buildEdges.contains(ret))
			return null;

		buildEdges.add(ret);
		return ret;
	}

	public IFSAutomaton<E> newBuild() throws FSAException
	{
		STATE initialState = makeState(automaton.getStart());
		STATE finalState   = makeState(automaton.getEnd());

		states = new HashSet<>(automaton.getGraph().vertexSet().size());
		finals = new HashSet<>();
		edges  = new ArrayList<>(automaton.getGraph().edgeSet().size());
		states.add(initialState);

		if (finalState != initialState)
			states.add(finalState);

		finals.add(finalState);

		if (automaton.getProperties().isSynchronous())
			build(automaton.getStart(), initialState);
		else if (mustBeSync)
			buildSync(automaton.getStart(), initialState);
		else
			build(automaton.getStart(), initialState);

		// Reindex states
//			int i = 0;
//			for(IFSAState<E> s : states)
//				((State<E>)s).id = i++;

		return builderFactory.get( //
			states, Collections.singletonList(initialState), finals, //
			edges, //
			automaton.getProperties(), //
			new GFSAValidation<E>());
	}

	private STATE build_addState(GCState fcurrent) throws FSAException
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

	private void build_addEdge(GCEdgeData edgeData, STATE acurrentState, STATE newAState) throws FSAException
	{
		IFSAEdge<E> newAEdge = makeEdge(edgeData, acurrentState, newAState);

		if (null == newAEdge)
			return;

		edges.add(newAEdge);
		acurrentState.childs.add(newAEdge);
	}

	private void build(GCState fstate, STATE acurrentState) throws FSAException
	{
		processedStates.add(fstate);
		Set<GCEdgeData> fedges = automaton.getGraph().outgoingEdgesOf(fstate);
		acurrentState.childs = new ArrayList<>(fedges.size());

		for (GCEdgeData edgeData : fedges)
		{
			GCState fcurrent  = automaton.getGraph().getEdgeTarget(edgeData);
			STATE   newAState = build_addState(fcurrent);
			build_addEdge(edgeData, acurrentState, newAState);
		}
	}

	private STATE build_addStateSync(GCState fcurrent) throws FSAException
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

	private void buildSync(GCState fstate, STATE acurrentState) throws FSAException
	{
		processedStates.add(fstate);
		Set<GCEdgeData> fedges = automaton.getGraph().outgoingEdgesOf(fstate);
		acurrentState.childs = new ArrayList<>(fedges.size());

		for (GCEdgeData edgeData : fedges)
		{
			if (edgeData.getType() == Type.EPSILON)
			{
				Collection<GCState> fstates = automaton.epsilonClosure(fstate);
				Collection<STATE>   astates = fstates.stream().map(s -> makeState(s)).collect(Collectors.toList());

				if (!Collections.disjoint(astates, finals))
					finals.add(makeState(fstate));

				Collection<GCEdgeData> edges = automaton.getEdges(fstates);

				for (GCEdgeData edge : edges)
				{
					/*
					 * Skip epsilon edges (not needed)
					 */
					if (edge.getType() == Type.EPSILON)
						continue;

					GCState ftarget   = automaton.getGraph().getEdgeTarget(edge);
					STATE   newAState = build_addStateSync(ftarget);
					build_addEdge(edge, acurrentState, newAState);
				}
			}
			else
			{
				GCState fcurrent  = automaton.getGraph().getEdgeTarget(edgeData);
				STATE   newAState = build_addStateSync(fcurrent);
				build_addEdge(edgeData, acurrentState, newAState);
			}
		}
	}

	@Override
	public String toString()
	{
		return automaton.toString();
	}

}
