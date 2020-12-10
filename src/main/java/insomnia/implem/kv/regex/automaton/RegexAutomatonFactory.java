package insomnia.implem.kv.regex.automaton;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;

import insomnia.FSA.FSAException;
import insomnia.FSA.IFSAutomaton;
import insomnia.implem.FSA.GCEdgeData;
import insomnia.implem.FSA.GCEdgeData.Type;
import insomnia.implem.FSA.GCState;
import insomnia.implem.FSA.GraphChunk;
import insomnia.implem.kv.regex.element.Const;
import insomnia.implem.kv.regex.element.IElement;
import insomnia.implem.kv.regex.element.Key;
import insomnia.implem.kv.regex.element.MultipleElement;
import insomnia.implem.kv.regex.element.OrElement;
import insomnia.implem.kv.regex.element.Quantifier;
import insomnia.implem.kv.regex.element.Regex;

public class RegexAutomatonFactory<E>
{
	private class GChunk extends GraphChunk
	{
		public GChunk()
		{
			super(null, null, graphSupplier());
		}

		public GChunk(GCState start, GCState end)
		{
			super(start, end, graphSupplier());
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
			getGraph().addVertex(vertex);
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
			getGraph().addEdge(sourceVertex, targetVertex, e);

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
	}

	private GChunk  automaton;
	private boolean mustBeSync = false;

	private int currentId = 0;

	private static Graph<GCState, GCEdgeData> graphSupplier()
	{
		return new DirectedPseudograph<>(null, null, false);
	}

	public RegexAutomatonFactory(IElement elements)// throws BuilderException
	{
		automaton = recursiveConstruct(elements);
	}

	public RegexAutomatonFactory<E> mustBeSync(boolean val)
	{
		mustBeSync = val;
		return this;
	}

	private GCState freshState()
	{
		return new GCState(currentId++);
	}

	public IFSAutomaton<E> newBuild() throws FSAException
	{
		return new RegexAutomatonBuilder<E>(automaton).mustBeSync(mustBeSync).newBuild();
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
			currentAutomaton = oneEdge(new GCEdgeData(key.getLabel(), GCEdgeData.Type.STRING_EQUALS));
		}
		else if (element instanceof Regex)
		{
			Regex regex = (Regex) element;
			currentAutomaton = oneEdge(new GCEdgeData(regex.getRegex(), GCEdgeData.Type.REGEX));
		}
		else if (element instanceof Const)
		{
			Const c = (Const) element;

			if (c.isNumber())
				currentAutomaton = oneEdge(new GCEdgeData(c.num, GCEdgeData.Type.NUMBER));
			else
				currentAutomaton = oneEdge(new GCEdgeData(c.str, GCEdgeData.Type.STRING_EQUALS));
		}
		else if (element instanceof OrElement)
		{
			OrElement orElement = (OrElement) element;

			List<GChunk> gcs = new ArrayList<>(orElement.size());

			for (IElement ie : orElement)
			{
				GChunk gc = recursiveConstruct(ie);

				if (gc.getGraph().inDegreeOf(gc.getStart()) > 0)
					gc.prependEdge(gc.addVertex(), new GCEdgeData(Type.EPSILON));

				if (gc.getGraph().outDegreeOf(gc.getEnd()) > 0)
					gc.appendEdge(gc.addVertex(), new GCEdgeData(Type.EPSILON));

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
				;
				currentAutomaton.addVertex(currentAutomaton.getStart());
				currentAutomaton.addVertex(currentAutomaton.getEnd());
				currentAutomaton.addEdge(currentAutomaton.getStart(), currentAutomaton.getEnd(), new GCEdgeData(GCEdgeData.Type.EPSILON));
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

				currentAutomaton.addEdge(currentAutomaton.getEnd(), currentAutomaton.getStart(), new GCEdgeData(GCEdgeData.Type.EPSILON));
			}
			else
			{
				if (sup < inf)
					throw new InvalidParameterException();
				if (sup != inf)
				{
					base.addEdge(base.getStart(), base.getEnd(), new GCEdgeData(GCEdgeData.Type.EPSILON));
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
		return automaton.getGraph().toString();
	}
}
