package insomnia.implem.kv.regex.automaton;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedPseudograph;

import insomnia.data.IPath;
import insomnia.data.ITree;
import insomnia.fsa.FSAException;
import insomnia.fsa.IFSAutomaton;
import insomnia.implem.fsa.gbuilder.GCEdgeData;
import insomnia.implem.fsa.gbuilder.GCEdgeData.Type;
import insomnia.implem.fsa.gbuilder.GCState;
import insomnia.implem.fsa.gbuilder.GraphChunk;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.fsa.KVGraphChunkModifier;
import insomnia.implem.kv.fsa.KVGraphChunkModifier.Environment;
import insomnia.implem.kv.regex.element.IElement;
import insomnia.implem.kv.regex.element.Key;
import insomnia.implem.kv.regex.element.MultipleElement;
import insomnia.implem.kv.regex.element.OrElement;
import insomnia.implem.kv.regex.element.Quantifier;
import insomnia.implem.kv.regex.element.Regex;
import insomnia.implem.kv.regex.element.Value;

/**
 * The factory to create an automaton from a parsed regex.
 * 
 * @author zuri
 * @param <V>
 * @param <E>
 */
public class RegexAutomatonFactory<V, E>
{
	private class GChunk extends GraphChunk
	{
		public GChunk()
		{
			super();
		}

		public GChunk(GCState start, GCState end)
		{
			super(start, end);
		}

		public GChunk copy()
		{
			GChunk cpy = new GChunk();
			copy(cpy);
			return cpy;
		}

		@Override
		public GCState freshState()
		{
			return RegexAutomatonFactory.this.freshState();
		}

		@Override
		public Graph<GCState, GCEdgeData> getGraph()
		{
			return super.getGraph();
		}

		public void cleanGraph()
		{
			setGraph(graphSupplier());
		}

		public GCState addVertex()
		{
			GCState state = freshState();
			addVertex(state);
			return state;
		}

		public void addVertex(GCState vertex)
		{
			addState(vertex);
		}

		public void prependEdge(GCState start, GCEdgeData e)
		{
			addEdge(start, getStart(), e);
			setStart(start);
		}

		public void appendEdge(GCState end, GCEdgeData e)
		{
			addEdge(getEnd(), end, e);
			setEnd(end);
		}

		public void addEdge(GCState sourceVertex, GCState targetVertex, GCEdgeData e)
		{
			super.addEdge(sourceVertex, targetVertex, e);

			if (e.getType() == Type.EPSILON)
				setProperties(getProperties().setSynchronous(false));
		}

		public void concat(GChunk b, int nb)
		{
			while (nb-- != 0)
			{
				concat(b);

				if (nb == 0)
					break;

				b = b.copy();
			}
		}

		@Override
		public GraphChunk create()
		{
			return new GChunk();
		}
	}

	private GChunk  automaton, modifiedAutomaton;
	private boolean mustBeSync = false;

	private int currentId = 0;

	private static Graph<GCState, GCEdgeData> graphSupplier()
	{
		return new DirectedPseudograph<>(null, null, false);
	}

	public RegexAutomatonFactory(IElement elements)// throws BuilderException
	{
		automaton         = recursiveConstruct(elements);
		modifiedAutomaton = automaton;
	}

	/**
	 * The graph chunk modifier may change the structure of the automaton before the build.
	 * The Consumer may be change between every build.
	 * 
	 * @param graphChunkModifier
	 * @return
	 */
	public RegexAutomatonFactory<V, E> setGraphChunkModifier(KVGraphChunkModifier graphChunkModifier)
	{
		if (graphChunkModifier == null)
			modifiedAutomaton = automaton;
		else
		{
			Environment env = new Environment()
			{

				@SuppressWarnings("unchecked")
				@Override
				public GraphChunk gluePath(GraphChunk gchunk, GCState start, GCState end, IPath<KVValue, KVLabel> path)
				{
					GChunk        ret    = new GChunk(start, end);
					List<KVLabel> labels = path.getLabels();
					int           nb     = labels.size();
					List<GCState> states = new ArrayList<GCState>(nb + 1);

					states.add(start);

					for (int i = 0; i < nb - 1; i++)
					{
						GCState state = gchunk.addState();
						ret.addState(state);
						states.add(state);
					}
					states.add(end);

					for (int i = 0; i < nb; i++)
						ret.addEdge(states.get(i), states.get(i + 1), GCEdgeData.createString(labels.get(i).toString()));

					Graphs.addGraph(((GChunk) gchunk).getGraph(), ret.getGraph());
					return ret;
				}
			};
			modifiedAutomaton = automaton.copy();
			graphChunkModifier.accept(modifiedAutomaton, env);
		}
		return this;
	}

	public RegexAutomatonFactory<V, E> mustBeSync(boolean val)
	{
		mustBeSync = val;
		return this;
	}

	private GCState freshState()
	{
		return new GCState(currentId++);
	}

	public IFSAutomaton<ITree<V, E>> newBuild() throws FSAException
	{
		return new RegexAutomatonBuilder<V, E>(modifiedAutomaton).mustBeSync(mustBeSync).newBuild();
	}

	private GChunk oneEdge(GCEdgeData edge)
	{
		GCState a, b;
		GChunk  ret = new GChunk(a = freshState(), b = freshState());
		ret.addVertex(a);
		ret.addVertex(b);
		ret.addEdge(a, b, edge);
		return ret;
	}

	private GChunk recursiveConstruct(IElement element)// throws BuilderException
	{
		GChunk currentAutomaton;

		if (element instanceof Key)
		{
			Key key = (Key) element;
			currentAutomaton = oneEdge(GCEdgeData.createString((key.getLabel())));
		}
		else if (element instanceof Regex)
		{
			Regex regex = (Regex) element;
			currentAutomaton = oneEdge(GCEdgeData.createRegex(regex.getRegex()));
		}
		else if (element instanceof Value)
		{
			Value c = (Value) element;
			currentAutomaton = oneEdge(GCEdgeData.createKVValue(c.getValue()));
		}
		else if (element instanceof OrElement)
		{
			OrElement orElement = (OrElement) element;

			List<GChunk> gcs = new ArrayList<>(orElement.size());

			for (IElement ie : orElement)
			{
				GChunk gc = recursiveConstruct(ie);

				if (gc.getNbParentEdges(gc.getStart()) > 0)
					gc.prependEdge(gc.addVertex(), GCEdgeData.createEpsilon());

				if (gc.getNbChildEdges(gc.getEnd()) > 0)
					gc.appendEdge(gc.addVertex(), GCEdgeData.createEpsilon());

				gcs.add(gc);
			}
			currentAutomaton = gcs.get(0);

			for (int i = 1, c = gcs.size(); i < c; i++)
				currentAutomaton.glue(gcs.get(i));
		}
		else if (element instanceof MultipleElement)
		{
			MultipleElement me = (MultipleElement) element;

			Iterator<IElement> iterator = me.iterator();
			currentAutomaton = recursiveConstruct(iterator.next());

			while (iterator.hasNext())
				currentAutomaton.concat(recursiveConstruct(iterator.next()));
		}
		else
			throw new InvalidParameterException("Invalid type for parameter");

		/*
		 * Make the quantifier
		 */
		Quantifier q = element.getQuantifier();

		int inf = q.getInf();
		int sup = q.getSup();

		if (inf != 1 || sup != 1)
		{
			GChunk base = currentAutomaton.copy();

			if (inf == 1)
				;
			else if (inf == 0)
			{
				currentAutomaton.cleanGraph();
				currentAutomaton.addVertex(currentAutomaton.getStart());
				currentAutomaton.addVertex(currentAutomaton.getEnd());
				currentAutomaton.addEdge(currentAutomaton.getStart(), currentAutomaton.getEnd(), GCEdgeData.createEpsilon());
			}
			else if (inf > 1)
				currentAutomaton.concat(base.copy(), inf - 1);
			else
				throw new InvalidParameterException("inf: " + inf);

			// Infty repeat
			if (sup == -1)
			{
				if (inf == 0)
					currentAutomaton.glue(base);

				currentAutomaton.addEdge(currentAutomaton.getEnd(), currentAutomaton.getStart(), GCEdgeData.createEpsilon());
			}
			else
			{
				if (sup < inf)
					throw new InvalidParameterException();
				if (sup != inf)
				{
					base.addEdge(base.getStart(), base.getEnd(), GCEdgeData.createEpsilon());
					GChunk repeat = base.copy();
					repeat.concat(base.copy(), sup - inf - 1);

					if (inf == 0)
						currentAutomaton.glue(repeat);
					else
						currentAutomaton.concat(base, sup - inf);
				}
			}
		}
		return currentAutomaton;
	}

	@Override
	public String toString()
	{
		return automaton.toString();
	}
}
