package insomnia.implem.fsa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import insomnia.fsa.IFSAProperties;
import insomnia.implem.fsa.FSAProperties;
import insomnia.implem.fsa.gbuilder.GCEdgeData.Type;

public abstract class GraphChunk
{
	private Graph<GCState, GCEdgeData> graph;

	private GCState start;
	private GCState end;

	private IFSAProperties properties;

	{
		// By default we don't know if it is deterministic
		properties = new FSAProperties(false, true);
	}

	public abstract GCState freshState();

	protected GraphChunk(GCState start, GCState end, Graph<GCState, GCEdgeData> graph)
	{
		this.start = start;
		this.end   = end;
		this.graph = graph;
	}

	public void setGraph(Graph<GCState, GCEdgeData> graph)
	{
		this.graph = graph;
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
	}

	protected void setEnd(GCState end)
	{
		this.end = end;
	}

	public Graph<GCState, GCEdgeData> getGraph()
	{
		return graph;
	}

	public void setProperties(IFSAProperties properties)
	{
		this.properties = properties;
	}

	public IFSAProperties getProperties()
	{
		return properties;
	}

	private int nbStates()
	{
		return graph.vertexSet().size();
	}

	public Collection<GCEdgeData> getEdges(Collection<GCState> states)
	{
		Collection<GCEdgeData> ret = new HashSet<GCEdgeData>();

		for (GCState state : states)
			ret.addAll(graph.outgoingEdgesOf(state));

		return new ArrayList<>(ret);
	}

	public Collection<GCState> epsilonClosure(GCState state)
	{
		return epsilonClosure(Collections.singleton(state));
	}

	public Collection<GCState> epsilonClosure(Collection<GCState> states)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<GCState>  ret         = new HashSet<>(nbStates());
		List<GCState> buffStates  = new ArrayList<>(nbStates());
		List<GCState> addedStates = new ArrayList<>(nbStates());

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
			ret.graph.addEdge(source, target, new GCEdgeData(edgeData));
		}
		return ret;
	}

	public void replaceState(GCState dest, GCState src)
	{
		List<GCEdgeData> edges = new ArrayList<>(graph.outgoingEdgesOf(src));

		for (GCEdgeData edgeData : edges)
		{
			GCState target = graph.getEdgeTarget(edgeData);
			graph.addEdge(dest, target, new GCEdgeData(edgeData));
		}
		// Copy to avoid concurent modification
		edges = new ArrayList<>(graph.incomingEdgesOf(src));

		for (GCEdgeData edgeData : edges)
		{
			GCState source = graph.getEdgeSource(edgeData);
			graph.addEdge(source, dest, new GCEdgeData(edgeData));
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