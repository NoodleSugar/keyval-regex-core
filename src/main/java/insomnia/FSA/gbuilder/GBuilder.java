package insomnia.FSA.gbuilder;

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

import insomnia.FSA.AbstractFSAEdge;
import insomnia.FSA.FSAException;
import insomnia.FSA.IFSAEdge;
import insomnia.FSA.IFSAState;
import insomnia.FSA.IFSAutomaton;
import insomnia.FSA.IGFSAutomaton;
import insomnia.FSA.algorithm.GFSAValidation;
import insomnia.implem.FSA.FSAEdgeEpsilon;
import insomnia.implem.FSA.FSAEdgeNumber;
import insomnia.implem.FSA.FSAEdgeRegex;
import insomnia.implem.FSA.FSAEdgeStringEq;
import insomnia.implem.FSA.GCEdgeData;
import insomnia.implem.FSA.GCEdgeData.Type;
import insomnia.implem.FSA.GCState;
import insomnia.implem.FSA.GraphChunk;

public class GBuilder<E, STATE extends GBuilderState<E>>
{
	Map<GCState, STATE> buildStates = new HashMap<>();

	Set<IFSAEdge<E>> buildEdges      = new HashSet<>();
	Set<GCState>     processedStates = new HashSet<>();

	GBuilderFSA<E> buildAutomaton;

	GraphChunk automaton;
	boolean    mustBeSync;

	Function<Integer, STATE> stateSupplier;

	GBuilderFSAFactory<E> builderFactory;

	public GBuilder(GraphChunk gc, Function<Integer, STATE> stateSupplier, GBuilderFSAFactory<E> builderFactory)
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

	public STATE makeState(GCState state)
	{
		STATE ret = buildStates.get(state);

		if (null != ret)
			return ret;

		ret = stateSupplier.apply(state.getId());
		buildStates.put(state, ret);
		return ret;
	}

	public AbstractFSAEdge<E> makeEdge(GCEdgeData edgeData, IFSAState<E> parent, IFSAState<E> child) throws FSAException
	{
		AbstractFSAEdge<E> ret;

		switch (edgeData.getType())
		{
		case EPSILON:
			ret = new FSAEdgeEpsilon<E>(parent, child);

			if (buildAutomaton.getProperties().isSynchronous())
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

		Collection<IFSAState<E>> states = new HashSet<>(automaton.getGraph().vertexSet().size());
		Collection<IFSAState<E>> finals = new HashSet<>();
		Collection<IFSAEdge<E>>  edges  = new ArrayList<>(automaton.getGraph().edgeSet().size());
		states.add(initialState);

		if (finalState != initialState)
			states.add(finalState);

		finals.add(finalState);
		buildAutomaton = builderFactory.get( //
			states, Collections.singletonList(initialState), finals, //
			edges, //
			automaton.getProperties(), //
			new GFSAValidation<E, IGFSAutomaton<E>>());

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

		return buildAutomaton;
	}

	private STATE build_addState(GCState fcurrent) throws FSAException
	{
		STATE newAState;

		if (processedStates.contains(fcurrent))
			newAState = buildStates.get(fcurrent);
		else
		{
			newAState = makeState(fcurrent);
			buildAutomaton.states.add(newAState);
			build(fcurrent, newAState);
		}
		return newAState;
	}

	private void build_addEdge(GCEdgeData edgeData, STATE acurrentState, STATE newAState) throws FSAException
	{
		IFSAEdge<E> newAEdge = makeEdge(edgeData, acurrentState, newAState);

		if (null == newAEdge)
			return;

		buildAutomaton.edges.add(newAEdge);
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
			buildAutomaton.states.add(newAState);
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

				if (!Collections.disjoint(astates, buildAutomaton.finalStates))
					buildAutomaton.finalStates.add(makeState(fstate));

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

}// END of Builder
