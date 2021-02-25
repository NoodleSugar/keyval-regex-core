package insomnia.implem.fsa.fpa.graphchunk;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedPseudograph;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatcher;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.AbstractGFPA;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.fsa.edge.FSAEdge;
import insomnia.implem.fsa.fpa.FPAProperties;
import insomnia.implem.fsa.fpa.GFPAMatchers;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;

/**
 * A graph representation of an automaton where node are {@link IGCState<VAL,LBL>} and edge are {@link IGCEdge<VAL,LBL>}.
 * This is a particular implementation for Automaton construction purposes.
 * A {@link GraphChunk} have identified and unique {@link GraphChunk#start} State and {@link GraphChunk#end} State.
 * <br>
 * <br>
 * Node: A graph chunk may be a chunk of another graph chunk.
 * 
 * @author zuri
 */
public final class GraphChunk<VAL, LBL> extends AbstractGFPA<VAL, LBL> implements IGFPA<VAL, LBL>
{

	// =========================================================================

	private Graph<IGCState<VAL, LBL>, IGCEdge<VAL, LBL>> graph;

	private IGCState<VAL, LBL> start;
	private IGCState<VAL, LBL> end;

	private IFPAProperties properties;

	// =========================================================================

	public GraphChunk(IFSAState<VAL, LBL> start, IFSAState<VAL, LBL> end)
	{
		this();
		this.start = (IGCState<VAL, LBL>) start;
		this.end   = (IGCState<VAL, LBL>) end;
		graph.addVertex(this.start);
		graph.addVertex(this.end);
	}

	public GraphChunk()
	{
		super();
		// By default we don't know if it is deterministic
		properties = new FPAProperties(false, true);
		cleanGraph();
	}

	@Override
	public IPathMatcher<VAL, LBL> matcher(IPath<VAL, LBL> element)
	{
		return GFPAMatchers.create(this, element);
	}

	public static <VAL, LBL> GraphChunk<VAL, LBL> createOneEdge(boolean isRooted, IFSALabelCondition<LBL> labelCondition, VAL aval, VAL bval)
	{
		GraphChunk<VAL, LBL>  ret      = new GraphChunk<>();
		IGCAFactory<VAL, LBL> afactory = ret.getAFactory();
		IFSAState<VAL, LBL>   a        = afactory.create(aval);
		IFSAState<VAL, LBL>   b        = afactory.create(bval);
		afactory.setRooted(a, isRooted);

		ret.setStart(a);
		ret.setEnd(b);
		ret.addEdge(a, b, labelCondition);
		return ret;
	}

	public static <VAL, LBL> GraphChunk<VAL, LBL> createOneState(boolean isRooted, boolean isTerminal, VAL value)
	{
		GraphChunk<VAL, LBL>  ret      = new GraphChunk<>();
		IGCAFactory<VAL, LBL> afactory = ret.getAFactory();
		IFSAState<VAL, LBL>   a        = afactory.create(value);
		afactory.setRooted(a, isRooted);
		afactory.setTerminal(a, isTerminal);
		ret.setStart(a);
		ret.setEnd(a);
		return ret;
	}

	/**
	 * Get a new {@link GraphChunk} without the looped "any" edges from start and end states.
	 */
	public GraphChunk<VAL, LBL> cleanLimits()
	{
		GraphChunk<VAL, LBL> gchunk = copyClone();

		for (IGCState<VAL, LBL> state : Arrays.asList(start, end))
		{
			for (IGCEdge<VAL, LBL> edge : gc_getEdgesOf(state))
			{
				if (FSALabelConditions.isTrueCondition(edge.getLabelCondition()) && this.graph.getEdgeTarget(edge).equals(state))
					gchunk.graph.removeEdge(edge);
			}
		}
		return gchunk;
	}

	// =========================================================================

	private static class AFactory<VAL, LBL> implements IGCAFactory<VAL, LBL>
	{
		@Override
		public IFSAState<VAL, LBL> create()
		{
			return GCStates.create();
		}

		@Override
		public IFSAState<VAL, LBL> create(VAL value)
		{
			return GCStates.create(value);
		}

		@Override
		public void setInitial(IFSAState<VAL, LBL> state, boolean v)
		{
			GCStates.setInitial((IGCState<VAL, LBL>) state, v);
		}

		@Override
		public void setFinal(IFSAState<VAL, LBL> state, boolean v)
		{
			GCStates.setFinal((IGCState<VAL, LBL>) state, v);
		}

		@Override
		public void setRooted(IFSAState<VAL, LBL> state, boolean v)
		{
			GCStates.setRooted((IGCState<VAL, LBL>) state, v);
		}

		@Override
		public void setTerminal(IFSAState<VAL, LBL> state, boolean v)
		{
			GCStates.setTerminal((IGCState<VAL, LBL>) state, v);
		}
	}

	/**
	 * For now, the factory is shared for all instance of {@link GraphChunk}.
	 * In the future each instance may be able to have its own factory.
	 */
	private static AFactory<?, ?> afactory = new AFactory<>();

	@SuppressWarnings("unchecked")
	private IGCAFactory<VAL, LBL> getAFactory()
	{
		return (IGCAFactory<VAL, LBL>) afactory;
	}

	// =========================================================================

	private Collection<IGCState<VAL, LBL>> gc_getStates()
	{
		return graph.vertexSet();
	}

	private Collection<IGCEdge<VAL, LBL>> gc_getEdgesOf(IGCState<VAL, LBL> state)
	{
		return gc_getEdgesOf(Collections.singleton(state));
	}

	private Collection<IGCEdge<VAL, LBL>> gc_getEdgesOf(Collection<IGCState<VAL, LBL>> states)
	{
		Collection<IGCEdge<VAL, LBL>> ret = new HashSet<IGCEdge<VAL, LBL>>();

		for (IGCState<VAL, LBL> state : states)
			ret.addAll(graph.outgoingEdgesOf(state));

		return ret;
	}

	private Collection<IGCEdge<VAL, LBL>> gc_getEdgesTo(Collection<IGCState<VAL, LBL>> states)
	{
		Collection<IGCEdge<VAL, LBL>> ret = new HashSet<IGCEdge<VAL, LBL>>();

		for (IGCState<VAL, LBL> state : states)
			ret.addAll(graph.incomingEdgesOf(state));

		return ret;
	}

	// =========================================================================

	public void cleanGraph()
	{
		graph = new DirectedPseudograph<>(null, null, false);
	}

	public IFSAState<VAL, LBL> getStart()
	{
		return start;
	}

	public IFSAState<VAL, LBL> getEnd()
	{
		return end;
	}

	public IGFPA<VAL, LBL> setProperties(IFPAProperties properties)
	{
		this.properties = properties;
		return this;
	}

	public IFPAProperties getProperties()
	{
		return properties;
	}

	public int nbEdges(IFSAState<VAL, LBL> state)
	{
		return graph.outDegreeOf((IGCState<VAL, LBL>) state);
	}

	public int nbParentEdges(IFSAState<VAL, LBL> state)
	{
		return graph.inDegreeOf((IGCState<VAL, LBL>) state);
	}

	public Collection<IGCEdge<VAL, LBL>> getLimitEdgesWithoutLoop(IFSAState<VAL, LBL> state)
	{
		IGCState<VAL, LBL>            fstate = (IGCState<VAL, LBL>) state;
		Collection<IGCEdge<VAL, LBL>> ret    = gc_getEdgesOf(fstate);

		if (state == getStart() || state == getEnd())
			ret.removeIf(edge -> this.graph.getEdgeTarget((IGCEdge<VAL, LBL>) edge).equals(fstate));

		return ret;
	}

	// =========================================================================

	private GraphChunk<VAL, LBL> create()
	{
		return new GraphChunk<>();
	}

	public void setStart(IFSAState<VAL, LBL> start)
	{
		this.start = (IGCState<VAL, LBL>) start;
		graph.addVertex(this.start);
	}

	public void setEnd(IFSAState<VAL, LBL> end)
	{
		this.end = (IGCState<VAL, LBL>) end;
		graph.addVertex(this.end);
	}

	public void addState(IFSAState<VAL, LBL> state)
	{
		graph.addVertex((IGCState<VAL, LBL>) state);
	}

	public void addEdge(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child, IFSALabelCondition<LBL> labelCondition)
	{
		graph.addVertex((IGCState<VAL, LBL>) parent);
		graph.addVertex((IGCState<VAL, LBL>) child);
		graph.addEdge((IGCState<VAL, LBL>) parent, (IGCState<VAL, LBL>) child, new GCEdges.GCEdge<>(labelCondition));

		// Is epsilon
		if (IFSALabelCondition.isEpsilon(labelCondition))
			setProperties(getProperties().setSynchronous(false));
	}

	public void prependEdge(IFSAState<VAL, LBL> start, IFSALabelCondition<LBL> labelCondition)
	{
		addEdge(start, (IGCState<VAL, LBL>) getStart(), labelCondition);
		this.start = (IGCState<VAL, LBL>) start;
	}

	public void appendEdge(IFSAState<VAL, LBL> end, IFSALabelCondition<LBL> labelCondition)
	{
		addEdge((IGCState<VAL, LBL>) getEnd(), end, labelCondition);
		this.end = (IGCState<VAL, LBL>) end;
	}

	public void setRooted(IFSAState<VAL, LBL> state, boolean v)
	{
		getAFactory().setRooted(state, v);
	}

	public void setTerminal(IFSAState<VAL, LBL> state, boolean v)
	{
		getAFactory().setTerminal(state, v);
	}

	public void setInitial(IFSAState<VAL, LBL> state, boolean v)
	{
		getAFactory().setInitial(state, v);
	}

	public void setFinal(IFSAState<VAL, LBL> state, boolean v)
	{
		getAFactory().setFinal(state, v);
	}

	public IFSAState<VAL, LBL> createState()
	{
		return getAFactory().create();
	}

	public IFSAState<VAL, LBL> createState(VAL value)
	{
		return getAFactory().create(value);
	}

	// =========================================================================

	/**
	 * Get all the states from 'state' such as 'labels' is a valid sequence.
	 * 
	 * @param state
	 * @return
	 */
	public Collection<GraphChunk<VAL, LBL>> getValidChunks(IFSAState<VAL, LBL> state, List<? extends LBL> labels)
	{
		assert (graph.containsVertex((IGCState<VAL, LBL>) state));
		Collection<GraphChunk<VAL, LBL>> nextChunks = new ArrayList<>();
		Collection<GraphChunk<VAL, LBL>> lastChunks = new ArrayList<>();
		{
			GraphChunk<VAL, LBL> tmpchunk = create();
			tmpchunk.start = (IGCState<VAL, LBL>) state;
			tmpchunk.end   = (IGCState<VAL, LBL>) state;
			nextChunks.add(tmpchunk);
		}

		for (LBL label : labels)
		{
			lastChunks.clear();
			lastChunks.addAll(nextChunks);
			nextChunks.clear();

			for (IGCState<VAL, LBL> lstate : lastChunks.stream().map((gc) -> gc.end).collect(Collectors.toList()))
			{
				for (IGCEdge<VAL, LBL> edge : graph.outgoingEdgesOf(lstate))
				{
					if (edge.getLabelCondition().test(label))
					{
						// Add the edge
						for (GraphChunk<VAL, LBL> chunk : lastChunks)
						{
							GraphChunk<VAL, LBL> cpy = chunk.copyClone();

							IGCState<VAL, LBL> lastEnd = cpy.end;
							IGCState<VAL, LBL> end     = graph.getEdgeTarget(edge);
							cpy.end = end;
							cpy.addEdge(lastEnd, end, edge.getLabelCondition());

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

	public Collection<GraphChunk<VAL, LBL>> getValidChunks_reverse(IFSAState<VAL, LBL> state, List<? extends LBL> labels)
	{
		assert (graph.containsVertex((IGCState<VAL, LBL>) state));
		Collection<GraphChunk<VAL, LBL>> nextChunks = new ArrayList<>();
		Collection<GraphChunk<VAL, LBL>> lastChunks = new ArrayList<>();
		{
			GraphChunk<VAL, LBL> tmpchunk = create();
			tmpchunk.start = (IGCState<VAL, LBL>) state;
			tmpchunk.end   = (IGCState<VAL, LBL>) state;
			nextChunks.add(tmpchunk);
		}

		for (LBL label : new IteratorIterable<>(new ReverseListIterator<>(labels)))
		{
			lastChunks.clear();
			lastChunks.addAll(nextChunks);
			nextChunks.clear();

			for (IGCState<VAL, LBL> lstate : lastChunks.stream().map((gc) -> gc.start).collect(Collectors.toList()))
			{
				for (IGCEdge<VAL, LBL> edge : graph.incomingEdgesOf(lstate))
				{
					if (edge.getLabelCondition().test(label))
					{
						// Add the edge
						for (GraphChunk<VAL, LBL> chunk : lastChunks)
						{
							GraphChunk<VAL, LBL> cpy = chunk.copyClone();

							IGCState<VAL, LBL> lastStart = cpy.start;
							IGCState<VAL, LBL> start     = graph.getEdgeSource(edge);
							cpy.start = start;
							cpy.addEdge(start, lastStart, edge.getLabelCondition());

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

	/**
	 * Get the first path chunk validating a sequence of labels between two states.
	 */
	public Optional<GraphChunk<VAL, LBL>> getFirstValidChunk(IFSAState<VAL, LBL> start, IFSAState<VAL, LBL> end, List<? extends LBL> labels)
	{
		Collection<GraphChunk<VAL, LBL>> chunks = getValidChunks(start, labels);
		return chunks.stream().filter((chunk) -> chunk.getEnd().equals(end)).findFirst();
	}

	// =========================================================================

	/**
	 * Return a copy of this with the same states and edges objects.
	 */
	public GraphChunk<VAL, LBL> copyClone()
	{
		return copyClone(create());
	}

	/**
	 * Copy this into 'ret'.<br>
	 * The copy copy exactly the same {@link IGCState<VAL,LBL>}s and {@link IGCEdge}s.
	 */
	private GraphChunk<VAL, LBL> copyClone(GraphChunk<VAL, LBL> ret)
	{
		ret.cleanGraph();
		ret.properties = properties;
		ret.start      = start;
		ret.end        = end;
		Graphs.addGraph(ret.graph, graph);
		return ret;
	}

	public GraphChunk<VAL, LBL> copy()
	{
		return copy(create());
	}

	/**
	 * Return a new state that is a copy of 'src' but not compare equals to 'src'.
	 */
	private IGCState<VAL, LBL> copyState(IGCState<VAL, LBL> src)
	{
		return GCStates.copy(src);
	}

	private IGCEdge<VAL, LBL> copyEdge(IGCEdge<VAL, LBL> src)
	{
		return GCEdges.copy(src);
	}

	/**
	 * Copy this into ret.<br>
	 * The copy create fresh {@link IGCState<VAL,LBL>}s.
	 * 
	 * @param ret The {@link GraphChunk<LBL,VAL>} which become the copy.
	 * @return A reference to ret.
	 */
	private GraphChunk<VAL, LBL> copy(GraphChunk<VAL, LBL> ret)
	{
		Set<IGCState<VAL, LBL>> graphNodes = graph.vertexSet();
		/**
		 * key: state of graph
		 * val: state of ret
		 */
		Map<IGCState<VAL, LBL>, IGCState<VAL, LBL>> states = new HashMap<>(graphNodes.size() * 2);

		for (IGCState<VAL, LBL> graphNode : graphNodes)
		{
			IGCState<VAL, LBL> newState = (IGCState<VAL, LBL>) copyState(graphNode);
			states.put(graphNode, newState);
			ret.graph.addVertex(newState);
		}
		ret.start      = states.get(start);
		ret.end        = states.get(end);
		ret.properties = properties;

		for (IGCEdge<VAL, LBL> edgeData : graph.edgeSet())
		{
			IGCState<VAL, LBL> source = states.get(graph.getEdgeSource(edgeData));
			IGCState<VAL, LBL> target = states.get(graph.getEdgeTarget(edgeData));
			ret.graph.addEdge(source, target, copyEdge(edgeData));
		}
		return ret;
	}

	// =========================================================================

	/**
	 * Replace the state src by dest.
	 * All the incoming/Outgoing edges of src are copied to dest.
	 * The state src is deleted at the end of the process.
	 * 
	 * @param dest
	 * @param src
	 */
	private void replaceState(IGCState<VAL, LBL> dest, IGCState<VAL, LBL> src)
	{
		List<IGCEdge<VAL, LBL>> edges = new ArrayList<>(graph.outgoingEdgesOf((IGCState<VAL, LBL>) src));

		for (IGCEdge<VAL, LBL> edgeData : edges)
		{
			IGCState<VAL, LBL> target = graph.getEdgeTarget(edgeData);
			graph.addEdge(dest, target, copyEdge(edgeData));
		}
		// Copy to avoid concurent modification
		edges = new ArrayList<>(graph.incomingEdgesOf(src));

		for (IGCEdge<VAL, LBL> edgeData : edges)
		{
			IGCState<VAL, LBL> source = graph.getEdgeSource(edgeData);
			graph.addEdge(source, dest, copyEdge(edgeData));
		}
		GCStates.merge(dest, src);
		graph.removeVertex(src);
	}

	public void add(GraphChunk<VAL, LBL> b)
	{
		Graphs.addGraph(graph, b.graph);
		properties = FPAProperties.union(properties, b.properties);
	}

	private void glue(IGCState<VAL, LBL> glue, GraphChunk<VAL, LBL> b)
	{
		Graphs.addGraph(graph, b.graph);
		replaceState(glue, b.start);
		properties = FPAProperties.union(properties, b.properties);
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

	public void concat(GraphChunk<VAL, LBL> b, int nb)
	{
		while (nb-- != 0)
		{
			concat(b);

			if (nb == 0)
				break;

			b = b.copy();
		}
	}

	/**
	 * Try to get a union with a pivot.
	 * A pivot is a common state between this and b.
	 */
	public void union(GraphChunk<VAL, LBL> b)
	{
		IGCState<VAL, LBL> start, end;

		if (b.end == this.start)
		{
			start = b.start;
			end   = this.end;
		}
		else if (b.start == this.end)
		{
			start = this.start;
			end   = b.end;
		}
		else
			throw new InvalidParameterException();

		union(b, start, end);
	}

	public void union(GraphChunk<VAL, LBL> b, IFSAState<VAL, LBL> start, IFSAState<VAL, LBL> end)
	{
		Graphs.addGraph(graph, b.graph);
		this.start = (IGCState<VAL, LBL>) start;
		this.end   = (IGCState<VAL, LBL>) end;

		setProperties(FPAProperties.union(getProperties(), b.getProperties()));
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
	private void glue(IGCState<VAL, LBL> gluea, IGCState<VAL, LBL> glueb, GraphChunk<VAL, LBL> b)
	{
		glue(gluea, b);
		replaceState(end, b.end);
	}

	// =========================================================================

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (!(obj instanceof GraphChunk))
			return false;

		@SuppressWarnings("unchecked")
		GraphChunk<VAL, LBL> b = (GraphChunk<VAL, LBL>) obj;

		return start.equals(b.start) && end.equals(b.end) //
			&& graph.equals(b.graph); //
	}

	@Override
	public int hashCode()
	{
		return start.hashCode() + end.hashCode() + graph.hashCode();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("GChunk: ").append(start).append(" ").append(end).append("\n");
		sb.append(super.toString());
		return sb.toString();
	}

	// =========================================================================
	// IGFPA methods

	private Map<IGCEdge<VAL, LBL>, IFSAEdge<VAL, LBL>> gc_as_fsa_edge = new WeakHashMap<>();

	private Collection<IFSAEdge<VAL, LBL>> gc_as_fsa_edge(Collection<IGCEdge<VAL, LBL>> edges)
	{
		Collection<IFSAEdge<VAL, LBL>> ret = new ArrayList<IFSAEdge<VAL, LBL>>(edges.size());

		for (IGCEdge<VAL, LBL> gcEdge : edges)
			ret.add(gc_as_fsa_edge.computeIfAbsent(gcEdge, //
				gce -> new FSAEdge<>(graph.getEdgeSource(gce), graph.getEdgeTarget(gce), gce.getLabelCondition())));

		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdges()
	{
		return gc_as_fsa_edge(graph.edgeSet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdgesOf(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return gc_as_fsa_edge(gc_getEdgesOf((Collection<IGCState<VAL, LBL>>) states));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IFSAEdge<VAL, LBL>> getAllEdgesTo(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		return gc_as_fsa_edge(gc_getEdgesTo((Collection<IGCState<VAL, LBL>>) states));
	}

	@Override
	public boolean isInitial(IFSAState<VAL, LBL> state)
	{
		return ((IGCState<VAL, LBL>) state).isInitial();
	}

	@Override
	public boolean isFinal(IFSAState<VAL, LBL> state)
	{
		return ((IGCState<VAL, LBL>) state).isFinal();
	}

	@Override
	public boolean isRooted(IFSAState<VAL, LBL> state)
	{
		return ((IGCState<VAL, LBL>) state).isRooted();
	}

	@Override
	public boolean isTerminal(IFSAState<VAL, LBL> state)
	{
		return ((IGCState<VAL, LBL>) state).isTerminal();
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getStates()
	{
		return new ArrayList<>(graph.vertexSet());
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getInitialStates()
	{
		return gc_getStates().stream().filter(s -> isInitial(s)).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getFinalStates()
	{
		return gc_getStates().stream().filter(s -> isFinal(s)).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getRootedStates()
	{
		return gc_getStates().stream().filter(s -> isRooted(s)).collect(Collectors.toList());
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getTerminalStates()
	{
		return gc_getStates().stream().filter(s -> isTerminal(s)).collect(Collectors.toList());
	}
}