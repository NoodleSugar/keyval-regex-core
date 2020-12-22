package insomnia.implem.fsa.graphchunk;

import java.util.ArrayList;
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
import insomnia.implem.fsa.graphchunk.GCEdgeData.Type;

/**
 * A graph representation of an automaton where node are {@link GCState} and edge are {@link GCEdgeData}.
 * This is a particular implementation for Automaton construction purposes.
 * A {@link GraphChunk} have identified and unique {@link GraphChunk#start} State and {@link GraphChunk#end} State.
 * <br>
 * <br>
 * Node: A graph chunk may be a chunk of another graph chunk.
 * 
 * @author zuri
 */
public abstract class GraphChunk
{
	private Graph<GCState, GCEdgeData> graph;

	private GCState start;
	private GCState end;

	private IFSAProperties properties;

	{
		// By default we don't know if it is deterministic
		properties = new FSAProperties(false, true);
		this.graph = new DirectedPseudograph<GCState, GCEdgeData>(null, null, false);
	}

	public abstract GCState freshState();

	public abstract GraphChunk create();

	protected GraphChunk(GCState start, GCState end)
	{
		this.start = start;
		this.end   = end;
		graph.addVertex(start);
		graph.addVertex(end);
	}

	public GraphChunk()
	{
	}

	protected void setGraph(Graph<GCState, GCEdgeData> graph)
	{
		this.graph = graph;
	}

	protected Graph<GCState, GCEdgeData> getGraph()
	{
		return graph;
	}

	public GCState getStart()
	{
		return start;
	}

	public GCState getEnd()
	{
		return end;
	}

	protected void setStart(GCState start)
	{
		this.start = start;
		graph.addVertex(start);
	}

	protected void setEnd(GCState end)
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

	public int getNbChildEdges(GCState state)
	{
		return graph.outDegreeOf(state);
	}

	public int getNbParentEdges(GCState state)
	{
		return graph.inDegreeOf(state);
	}

	public GCState edge_getStart(GCEdgeData edge)
	{
		return graph.getEdgeSource(edge);
	}

	public GCState edge_getEnd(GCEdgeData edge)
	{
		return graph.getEdgeTarget(edge);
	}

	public Collection<GCEdgeData> getEdges(GCState state)
	{
		return getEdges(Collections.singleton(state));
	}

	public Collection<GCEdgeData> getEdges(Collection<GCState> states)
	{
		Collection<GCEdgeData> ret = new HashSet<GCEdgeData>();

		for (GCState state : states)
			ret.addAll(graph.outgoingEdgesOf(state));

		return new ArrayList<>(ret);
	}

	public GCState addState()
	{
		GCState ret = freshState();
		graph.addVertex(ret);
		return ret;
	}

	public void addState(GCState state)
	{
		graph.addVertex(state);
	}

	public void addEdge(GCState parent, GCState child, GCEdgeData data)
	{
		graph.addEdge(parent, child, data);
	}

	/**
	 * Get all the states from 'state' such as 'labels' is a valid sequence.
	 * 
	 * @param state
	 * @return
	 */
	public Collection<GraphChunk> validStates(GCState state, List<? extends Object> labels)
	{
		Collection<GraphChunk> nextChunks = new ArrayList<>();
		Collection<GraphChunk> lastChunks = new ArrayList<>();
		{
			GraphChunk tmpchunk = create();
			tmpchunk.setStart(state);
			tmpchunk.setEnd(state);
			nextChunks.add(tmpchunk);
		}

		for (Object label : labels)
		{
			lastChunks.clear();
			lastChunks.addAll(nextChunks);
			nextChunks.clear();

			for (GCState lstate : lastChunks.stream().map((gc) -> gc.getEnd()).collect(Collectors.toList()))
			{
				for (GCEdgeData edge : graph.outgoingEdgesOf(lstate))
				{
					if (edge.test(label))
					{
						// Add the edge
						for (GraphChunk chunk : lastChunks)
						{
							GraphChunk cpy = chunk.copyClone();

							GCState lastEnd = cpy.getEnd();
							GCState end     = graph.getEdgeTarget(edge);
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

	public Collection<GraphChunk> validStates_reverse(GCState state, List<? extends Object> labels)
	{
		Collection<GraphChunk> nextChunks = new ArrayList<>();
		Collection<GraphChunk> lastChunks = new ArrayList<>();
		{
			GraphChunk tmpchunk = create();
			tmpchunk.setStart(state);
			tmpchunk.setEnd(state);
			nextChunks.add(tmpchunk);
		}

		for (Object label : new IteratorIterable<>(new ReverseListIterator<>(labels)))
		{
			lastChunks.clear();
			lastChunks.addAll(nextChunks);
			nextChunks.clear();

			for (GCState lstate : lastChunks.stream().map((gc) -> gc.getStart()).collect(Collectors.toList()))
			{
				for (GCEdgeData edge : graph.incomingEdgesOf(lstate))
				{
					if (edge.test(label))
					{
						// Add the edge
						for (GraphChunk chunk : lastChunks)
						{
							GraphChunk cpy = chunk.copyClone();

							GCState lastStart = cpy.getStart();
							GCState start     = graph.getEdgeSource(edge);
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

	public Collection<GCState> epsilonClosure(GCState state)
	{
		return epsilonClosure(Collections.singleton(state));
	}

	public Collection<GCState> epsilonClosure(Collection<GCState> states)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<GCState>  ret         = new HashSet<>(getNbStates());
		List<GCState> buffStates  = new ArrayList<>(getNbStates());
		List<GCState> addedStates = new ArrayList<>(getNbStates());

		ret.addAll(states);
		buffStates.addAll(states);

		while (!buffStates.isEmpty())
		{
			for (GCEdgeData edge : getEdges(buffStates))
			{
				GCState target = graph.getEdgeTarget(edge);

				if (edge.getType() == Type.EPSILON && !ret.contains(target))
					addedStates.add(target);
			}
			buffStates.clear();
			buffStates.addAll(addedStates);
			ret.addAll(addedStates);
			addedStates.clear();
		}
		return new ArrayList<>(ret);
	}

	protected GraphChunk copyClone()
	{
		return copyClone(create());
	}

	/**
	 * Copy this into 'ret'.<br>
	 * The copy copy exactly the same {@link GCState}s.
	 * 
	 * @param ret
	 * @return
	 */
	protected GraphChunk copyClone(GraphChunk ret)
	{
		ret.start = start;
		ret.end   = end;
		Graphs.addGraph(ret.graph, graph);
		return ret;
	}

	/**
	 * Copy this into ret.<br>
	 * The copy create fresh {@link GCState}s.
	 * 
	 * @param ret The {@link GraphChunk} which become the copy.
	 * @return A reference to ret.
	 */
	public GraphChunk copy(GraphChunk ret)
	{
		Set<GCState> graphNodes = graph.vertexSet();
		/**
		 * key: state of graph
		 * val: state of ret
		 */
		Map<GCState, GCState> states = new HashMap<>(graphNodes.size() * 2);

		for (GCState graphNode : graphNodes)
		{
			GCState newState = freshState();
			states.put(graphNode, newState);
			ret.graph.addVertex(newState);
		}
		ret.start      = states.get(start);
		ret.end        = states.get(end);
		ret.properties = properties;

		for (GCEdgeData edgeData : graph.edgeSet())
		{
			GCState source = states.get(graph.getEdgeSource(edgeData));
			GCState target = states.get(graph.getEdgeTarget(edgeData));
			ret.graph.addEdge(source, target, GCEdgeData.copy(edgeData));
		}
		return ret;
	}

	public void replaceState(GCState dest, GCState src)
	{
		List<GCEdgeData> edges = new ArrayList<>(graph.outgoingEdgesOf(src));

		for (GCEdgeData edgeData : edges)
		{
			GCState target = graph.getEdgeTarget(edgeData);
			graph.addEdge(dest, target, GCEdgeData.copy(edgeData));
		}
		// Copy to avoid concurent modification
		edges = new ArrayList<>(graph.incomingEdgesOf(src));

		for (GCEdgeData edgeData : edges)
		{
			GCState source = graph.getEdgeSource(edgeData);
			graph.addEdge(source, dest, GCEdgeData.copy(edgeData));
		}
		graph.removeVertex(src);
	}

	public void glue(GCState glue, GraphChunk b)
	{
		Graphs.addGraph(graph, b.graph);
		replaceState(glue, b.start);
		properties = FSAProperties.union(properties, b.properties);
	}

	public void concat(GraphChunk b)
	{
		glue(end, b);
		end = b.end;
	}

	public void glue(GraphChunk b)
	{
		glue(start, end, b);
	}

	public void glue(GCState gluea, GCState glueb, GraphChunk b)
	{
		glue(gluea, b);
		replaceState(end, b.end);
	}

	@Override
	public String toString()
	{
		return graph.toString();
	}
}