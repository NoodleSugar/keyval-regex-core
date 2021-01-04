package insomnia.implem.fsa.graphchunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedPseudograph;

import insomnia.fsa.IFSAProperties;
import insomnia.implem.fsa.FSAProperties;

/**
 * A graph representation of an automaton where node are {@link IGCState<VAL>} and edge are {@link IGCEdge<LBL>}.
 * This is a particular implementation for Automaton construction purposes.
 * A {@link GraphChunk} have identified and unique {@link GraphChunk#start} State and {@link GraphChunk#end} State.
 * <br>
 * <br>
 * Node: A graph chunk may be a chunk of another graph chunk.
 * 
 * @author zuri
 */
public abstract class GraphChunk<VAL, LBL>
{
	private Graph<IGCState<VAL>, IGCEdge<LBL>> graph;

	private Collection<IGCState<VAL>> finalStates;

	private IGCState<VAL> start;
	private IGCState<VAL> end;

	private IFSAProperties properties;

	// =========================================================================

	{
		// By default we don't know if it is deterministic
		properties  = new FSAProperties(false, true);
		this.graph  = new DirectedPseudograph<IGCState<VAL>, IGCEdge<LBL>>(null, null, false);
		finalStates = new HashSet<>();
	}

	// =========================================================================

	/**
	 * Return a state with a new id that is a copy of 'src'.
	 * 
	 * @param src
	 * @return
	 */
	public abstract IGCState<VAL> copyState(IGCState<VAL> src);

	public abstract IGCEdge<LBL> copyEdge(IGCEdge<LBL> src);

	public abstract GraphChunk<VAL, LBL> create();

	// =========================================================================

	protected GraphChunk(IGCState<VAL> start, IGCState<VAL> end)
	{
		this.start = start;
		this.end   = end;
		graph.addVertex(start);
		graph.addVertex(end);
	}

	public GraphChunk()
	{
	}

	public GraphChunk<VAL, LBL> cleanLimits()
	{
		GraphChunk<VAL, LBL> gchunk = copyClone();

		for (IGCState<VAL> state : Arrays.asList(getStart(), getEnd()))
		{
			for (IGCEdge<LBL> edge : getEdges(state))
			{
				if (GCEdges.isAny(edge) && this.edge_getEnd(edge).equals(state))
					gchunk.graph.removeEdge(edge);
			}
		}
		return gchunk;
	}

	// =========================================================================

	protected void setGraph(Graph<IGCState<VAL>, IGCEdge<LBL>> graph)
	{
		this.graph = graph;
	}

	protected Graph<IGCState<VAL>, IGCEdge<LBL>> getGraph()
	{
		return graph;
	}

	public Collection<IGCState<VAL>> getStates()
	{
		return graph.vertexSet();
	}

	public IGCState<VAL> getStart()
	{
		return start;
	}

	public IGCState<VAL> getEnd()
	{
		return end;
	}

	public Collection<IGCState<VAL>> getFinalStates()
	{
		return Collections.unmodifiableCollection(finalStates);
	}

	public void setStateFinal(IGCState<VAL> state)
	{
		finalStates.add(state);
	}

	protected void setStart(IGCState<VAL> start)
	{
		this.start = start;
		graph.addVertex(start);
	}

	protected void setEnd(IGCState<VAL> end)
	{
		this.end = end;
		graph.addVertex(end);
	}

	public void setProperties(IFSAProperties properties)
	{
		this.properties = properties;
	}

	public IFSAProperties getProperties()
	{
		return properties;
	}

	public int getNbStates()
	{
		return graph.vertexSet().size();
	}

	public int getNbEdges()
	{
		return graph.edgeSet().size();
	}

	public int getNbChildEdges(IGCState<VAL> state)
	{
		return graph.outDegreeOf(state);
	}

	public int getNbParentEdges(IGCState<VAL> state)
	{
		return graph.inDegreeOf(state);
	}

	public IGCState<VAL> edge_getStart(IGCEdge<LBL> edge)
	{
		return graph.getEdgeSource(edge);
	}

	public IGCState<VAL> edge_getEnd(IGCEdge<LBL> edge)
	{
		return graph.getEdgeTarget(edge);
	}

	public Collection<IGCEdge<LBL>> getEdges(IGCState<VAL> state)
	{
		return getEdges(Collections.singleton(state));
	}

	public Collection<IGCEdge<LBL>> getLimitEdgesWithoutLoop(IGCState<VAL> state)
	{
		IGCState<VAL>            fstate = state;
		Collection<IGCEdge<LBL>> ret    = getEdges(state);

		if (state == getStart() || state == getEnd())
			ret.removeIf(edge -> this.edge_getEnd(edge).equals(fstate));

		return ret;
	}

	public Collection<IGCEdge<LBL>> getEdges(Collection<IGCState<VAL>> states)
	{
		Collection<IGCEdge<LBL>> ret = new HashSet<IGCEdge<LBL>>();

		for (IGCState<VAL> state : states)
			ret.addAll(graph.outgoingEdgesOf(state));

		return new ArrayList<>(ret);
	}

	public void addState(IGCState<VAL> state)
	{
		graph.addVertex(state);
	}

	public void addEdge(IGCState<VAL> parent, IGCState<VAL> child, IGCEdge<LBL> data)
	{
		graph.addEdge(parent, child, data);
	}

	/**
	 * Get all the states from 'state' such as 'labels' is a valid sequence.
	 * 
	 * @param state
	 * @return
	 */
	public Collection<GraphChunk<VAL, LBL>> validStates(IGCState<VAL> state, List<? extends LBL> labels)
	{
		Collection<GraphChunk<VAL, LBL>> nextChunks = new ArrayList<>();
		Collection<GraphChunk<VAL, LBL>> lastChunks = new ArrayList<>();
		{
			GraphChunk<VAL, LBL> tmpchunk = create();
			tmpchunk.setStart(state);
			tmpchunk.setEnd(state);
			nextChunks.add(tmpchunk);
		}

		for (LBL label : labels)
		{
			lastChunks.clear();
			lastChunks.addAll(nextChunks);
			nextChunks.clear();

			for (IGCState<VAL> lstate : lastChunks.stream().map((gc) -> gc.getEnd()).collect(Collectors.toList()))
			{
				for (IGCEdge<LBL> edge : graph.outgoingEdgesOf(lstate))
				{
					if (edge.test(label))
					{
						// Add the edge
						for (GraphChunk<VAL, LBL> chunk : lastChunks)
						{
							GraphChunk<VAL, LBL> cpy = chunk.copyClone();

							IGCState<VAL> lastEnd = cpy.getEnd();
							IGCState<VAL> end     = graph.getEdgeTarget(edge);
							cpy.setEnd(end);
							cpy.addEdge(lastEnd, end, edge);

							nextChunks.add(cpy);
						}
					}
				}
			}
			if (nextChunks.isEmpty())
				return Collections.emptyList();
		}
		return nextChunks;
	}

	public Collection<GraphChunk<VAL, LBL>> validStates_reverse(IGCState<VAL> state, List<? extends LBL> labels)
	{
		Collection<GraphChunk<VAL, LBL>> nextChunks = new ArrayList<>();
		Collection<GraphChunk<VAL, LBL>> lastChunks = new ArrayList<>();
		{
			GraphChunk<VAL, LBL> tmpchunk = create();
			tmpchunk.setStart(state);
			tmpchunk.setEnd(state);
			nextChunks.add(tmpchunk);
		}

		for (LBL label : new IteratorIterable<>(new ReverseListIterator<>(labels)))
		{
			lastChunks.clear();
			lastChunks.addAll(nextChunks);
			nextChunks.clear();

			for (IGCState<VAL> lstate : lastChunks.stream().map((gc) -> gc.getStart()).collect(Collectors.toList()))
			{
				for (IGCEdge<LBL> edge : graph.incomingEdgesOf(lstate))
				{
					if (edge.test(label))
					{
						// Add the edge
						for (GraphChunk<VAL, LBL> chunk : lastChunks)
						{
							GraphChunk<VAL, LBL> cpy = chunk.copyClone();

							IGCState<VAL> lastStart = cpy.getStart();
							IGCState<VAL> start     = graph.getEdgeSource(edge);
							cpy.setStart(start);
							cpy.addEdge(start, lastStart, edge);

							nextChunks.add(cpy);
						}
					}
				}
			}
			if (nextChunks.isEmpty())
				return Collections.emptyList();
		}
		return nextChunks;
	}

	public Collection<IGCState<VAL>> epsilonClosure(IGCState<VAL> state)
	{
		return epsilonClosure(Collections.singleton(state));
	}

	public Collection<IGCState<VAL>> epsilonClosure(Collection<IGCState<VAL>> states)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<IGCState<VAL>>  ret         = new HashSet<>(getNbStates());
		List<IGCState<VAL>> buffStates  = new ArrayList<>(getNbStates());
		List<IGCState<VAL>> addedStates = new ArrayList<>(getNbStates());

		ret.addAll(states);
		buffStates.addAll(states);

		while (!buffStates.isEmpty())
		{
			for (IGCEdge<LBL> edge : getEdges(buffStates))
			{
				IGCState<VAL> target = graph.getEdgeTarget(edge);

				if (GCEdges.isEpsilon(edge) && !ret.contains(target))
					addedStates.add(target);
			}
			buffStates.clear();
			buffStates.addAll(addedStates);
			ret.addAll(addedStates);
			addedStates.clear();
		}
		return new ArrayList<>(ret);
	}

	protected GraphChunk<VAL, LBL> copyClone()
	{
		return copyClone(create());
	}

	/**
	 * Copy this into 'ret'.<br>
	 * The copy copy exactly the same {@link IGCState<VAL>}s.
	 * 
	 * @param ret
	 * @return
	 */
	protected GraphChunk<VAL, LBL> copyClone(GraphChunk<VAL, LBL> ret)
	{
		ret.start = start;
		ret.end   = end;
		Graphs.addGraph(ret.graph, graph);
		return ret;
	}

	/**
	 * Copy this into ret.<br>
	 * The copy create fresh {@link IGCState<VAL>}s.
	 * 
	 * @param ret The {@link GraphChunk<LBL,VAL>} which become the copy.
	 * @return A reference to ret.
	 */
	public GraphChunk<VAL, LBL> copy(GraphChunk<VAL, LBL> ret)
	{
		Set<IGCState<VAL>> graphNodes = graph.vertexSet();
		/**
		 * key: state of graph
		 * val: state of ret
		 */
		Map<IGCState<VAL>, IGCState<VAL>> states = new HashMap<>(graphNodes.size() * 2);

		for (IGCState<VAL> graphNode : graphNodes)
		{
			IGCState<VAL> newState = copyState(graphNode);
			states.put(graphNode, newState);
			ret.graph.addVertex(newState);
		}
		ret.start      = states.get(start);
		ret.end        = states.get(end);
		ret.properties = properties;

		for (IGCEdge<LBL> edgeData : graph.edgeSet())
		{
			IGCState<VAL> source = states.get(graph.getEdgeSource(edgeData));
			IGCState<VAL> target = states.get(graph.getEdgeTarget(edgeData));
			ret.graph.addEdge(source, target, copyEdge(edgeData));
		}
		return ret;
	}

	/**
	 * Replace the state src by dest.
	 * All the incoming/Outgoing edges of src are copied to dest.
	 * The state src is deleted at the end of the process.
	 * 
	 * @param dest
	 * @param src
	 */
	private void replaceState(IGCState<VAL> dest, IGCState<VAL> src)
	{
		List<IGCEdge<LBL>> edges = new ArrayList<>(graph.outgoingEdgesOf(src));

		for (IGCEdge<LBL> edgeData : edges)
		{
			IGCState<VAL> target = graph.getEdgeTarget(edgeData);
			graph.addEdge(dest, target, copyEdge(edgeData));
		}
		// Copy to avoid concurent modification
		edges = new ArrayList<>(graph.incomingEdgesOf(src));

		for (IGCEdge<LBL> edgeData : edges)
		{
			IGCState<VAL> source = graph.getEdgeSource(edgeData);
			graph.addEdge(source, dest, copyEdge(edgeData));
		}
		GCStates.copy(dest, src, dest.getId());
		graph.removeVertex(src);
	}

	public void glue(IGCState<VAL> glue, GraphChunk<VAL, LBL> b)
	{
		Graphs.addGraph(graph, b.graph);
		replaceState(glue, b.start);
		properties = FSAProperties.union(properties, b.properties);
	}

	/**
	 * Glue b to the end of this and set this.end to b.end.
	 * 
	 * @param b
	 */
	public void concat(GraphChunk<VAL, LBL> b)
	{
		glue(end, b);

		// b is not only a one state graph
		if (b.start != b.end)
			end = b.end;
	}

	/**
	 * Glue the chunk b to this at its start and end states.
	 * 
	 * @param b
	 */
	public void glue(GraphChunk<VAL, LBL> b)
	{
		glue(start, end, b);
	}

	/**
	 * Glue the chunk b to this with b.start glue to gluea and b.end glue to glueb.
	 * 
	 * @param gluea
	 * @param glueb
	 * @param b
	 */
	public void glue(IGCState<VAL> gluea, IGCState<VAL> glueb, GraphChunk<VAL, LBL> b)
	{
		glue(gluea, b);
		replaceState(end, b.end);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("GChunk: ").append(start).append(" ").append(end).append("\n");
		sb.append("Nodes: ").append(graph.vertexSet()).append("\n");
		sb.append("Edges: \n");

		graph.edgeSet().forEach((e) -> //
		sb.append(graph.getEdgeSource(e)) //
			.append(" ") //
			.append(e.toString()).append(" ") //
			.append(graph.getEdgeTarget(e)) //
			.append("\n"));

		return sb.toString();
	}
}