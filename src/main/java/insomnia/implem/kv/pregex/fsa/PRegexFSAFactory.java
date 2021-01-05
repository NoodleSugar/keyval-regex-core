package insomnia.implem.kv.pregex.fsa;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.fsa.FSAException;
import insomnia.fsa.IFSAutomaton;
import insomnia.implem.fsa.graphchunk.GCEdges;
import insomnia.implem.fsa.graphchunk.GCStates;
import insomnia.implem.fsa.graphchunk.GraphChunk;
import insomnia.implem.fsa.graphchunk.IGCEdge;
import insomnia.implem.fsa.graphchunk.IGCState;
import insomnia.implem.kv.fsa.KVGraphChunkModifier;
import insomnia.implem.kv.fsa.KVGraphChunkModifier.Environment;
import insomnia.implem.kv.pregex.IPRegexElement;
import insomnia.implem.kv.pregex.PRegexElements.Disjunction;
import insomnia.implem.kv.pregex.PRegexElements.Key;
import insomnia.implem.kv.pregex.PRegexElements.Regex;
import insomnia.implem.kv.pregex.PRegexElements.Sequence;
import insomnia.implem.kv.pregex.PRegexElements.Value;
import insomnia.implem.kv.pregex.Quantifier;
import insomnia.implem.kv.pregex.fsa.PRegexFSABuilder.AbstractFSA;

/**
 * The factory to create an automaton from a parsed regex.
 * 
 * @author zuri
 * @param <V>
 * @param <E>
 */
public class PRegexFSAFactory<VAL, LBL>
{
	private class GChunk extends GraphChunk<VAL, LBL>
	{
		public GChunk()
		{
			super();
		}

		public GChunk(IGCState<VAL> start, IGCState<VAL> end)
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
		public Graph<IGCState<VAL>, IGCEdge<LBL>> getGraph()
		{
			return super.getGraph();
		}

		public IGCState<VAL> addVertex()
		{
			IGCState<VAL> state = freshState();
			addVertex(state);
			return state;
		}

		public void addVertex(IGCState<VAL> vertex)
		{
			addState(vertex);
		}

		public void prependEdge(IGCState<VAL> start, IGCEdge<LBL> e)
		{
			addEdge(start, getStart(), e);
			setStart(start);
		}

		public void appendEdge(IGCState<VAL> end, IGCEdge<LBL> e)
		{
			addEdge(getEnd(), end, e);
			setEnd(end);
		}

		public void addEdge(IGCState<VAL> sourceVertex, IGCState<VAL> targetVertex, IGCEdge<LBL> e)
		{
			super.addEdge(sourceVertex, targetVertex, e);

			if (GCEdges.isEpsilon(e))
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
		public GraphChunk<VAL, LBL> create()
		{
			return new GChunk();
		}

		@Override
		public IGCState<VAL> copyState(IGCState<VAL> src)
		{
			return GCStates.copy(src, nextStateId());
		}

		@Override
		public IGCEdge<LBL> copyEdge(IGCEdge<LBL> src)
		{
			return GCEdges.copy(src);
		}

		@Override
		protected void setStart(IGCState<VAL> start)
		{
			super.setStart(start);
		}

		@Override
		protected void setEnd(IGCState<VAL> end)
		{
			super.setEnd(end);
		}
	}

	// =========================================================================

	private GChunk  automaton, modifiedAutomaton;
	private boolean mustBeSync = false;

	private int currentId = 0;

	public PRegexFSAFactory(IPRegexElement elements)// throws BuilderException
	{
		automaton         = recursiveConstruct(elements);
		modifiedAutomaton = automaton;

		// Not set in recursiveConstruct()
		GCStates.setInitial(automaton.getStart(), true);
		GCStates.setFinal(automaton.getEnd(), true);
	}

	/**
	 * Construct the factory from a path.
	 * 
	 * @param path
	 */
	public PRegexFSAFactory(IPath<VAL, LBL> path)
	{
		automaton         = constructFromPath(path);
		modifiedAutomaton = automaton;
	}

	/**
	 * The graph chunk modifier may change the structure of the automaton before the build.
	 * The Consumer may be change between every build.
	 * 
	 * @param graphChunkModifier
	 * @return
	 */
	public PRegexFSAFactory<VAL, LBL> setGraphChunkModifier(KVGraphChunkModifier<VAL, LBL> graphChunkModifier)
	{
		if (graphChunkModifier == null)
			modifiedAutomaton = automaton;
		else
		{
			Environment<VAL, LBL> env = new Environment<VAL, LBL>()
			{
				@Override
				public GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IGCState<VAL> start, IGCState<VAL> end, IPath<VAL, LBL> path)
				{
					GChunk              ret    = new GChunk(start, end);
					List<LBL>           labels = path.getLabels();
					int                 nb     = labels.size();
					List<IGCState<VAL>> states = new ArrayList<IGCState<VAL>>(nb + 1);

					states.add(start);

					// Add the states
					for (int i = 0; i < nb - 1; i++)
					{
						IGCState<VAL> state = GCStates.create(nextStateId());
						gchunk.addState(state);
						ret.addState(state);
						states.add(state);
					}
					states.add(end);

					// Add the edges
					for (int i = 0; i < nb; i++)
						ret.addEdge(states.get(i), states.get(i + 1), GCEdges.createStringEq(labels.get(i).toString()));

					Graphs.addGraph(((GChunk) gchunk).getGraph(), ret.getGraph());
					return ret;
				}

				@Override
				public GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IGCState<VAL> start, IPath<VAL, LBL> path)
				{
					IGCState<VAL> end = GCStates.create(nextStateId(), path.getValue());
					gchunk.addState(end);

					GCStates.setFinal(end);
					GCStates.setTerminal(end, path.isTerminal());

					if (!path.isTerminal())
						gchunk.addEdge(end, end, GCEdges.createAny());

					return gluePath(gchunk, start, end, path);
				}
			};
			modifiedAutomaton = automaton.copy();
			graphChunkModifier.accept(modifiedAutomaton, env);
		}
		return this;
	}

	public PRegexFSAFactory<VAL, LBL> mustBeSync(boolean val)
	{
		mustBeSync = val;
		return this;
	}

	private int nextStateId()
	{
		return currentId++;
	}

	private IGCState<VAL> freshState()
	{
		return GCStates.create(nextStateId());
	}

	public IFSAutomaton<VAL, LBL> newBuild() throws FSAException
	{
		AbstractFSA<VAL, LBL> fsa = (AbstractFSA<VAL, LBL>) new PRegexFSABuilder<VAL, LBL>(modifiedAutomaton).mustBeSync(mustBeSync).newBuild();
		return fsa;
	}

	private GChunk oneEdge(IGCEdge<LBL> edge)
	{
		IGCState<VAL> a, b;
		GChunk        ret = new GChunk(a = freshState(), b = freshState());
		ret.addVertex(a);
		ret.addVertex(b);
		ret.addEdge(a, b, edge);
		return ret;
	}

	private GChunk terminalState(VAL value)
	{
		IGCState<VAL> a = GCStates.create(nextStateId(), Optional.ofNullable(value));
		GCStates.setTerminal(a, true);
		GCStates.setFinal(a, true);

		GChunk ret = new GChunk(a, a);
		return ret;
	}

	private GChunk constructFromPath(IPath<VAL, LBL> path)
	{
		GChunk currentAutomaton = new GChunk();

		if (path.isEmpty())
			return currentAutomaton;

		INode<VAL, LBL> state = path.getRoot();
		IGCState<VAL>   lastGCState;

		Optional<IEdge<VAL, LBL>> optChild = path.getChild(state);

		{
			IGCState<VAL> newGCState = GCStates.create(0, state.getValue());
			GCStates.setRooted(newGCState, path.isRooted());
			GCStates.setInitial(newGCState, true);

			currentAutomaton.setStart(newGCState);
			lastGCState = newGCState;
		}

		if (optChild.isPresent())
		{
			for (int id = 1;; id++)
			{
				state = optChild.get().getChild();
				Optional<VAL> value = state.getValue();

				Optional<IEdge<VAL, LBL>> nextOptChild = path.getChild(state);

				IGCState<VAL> newGCState = GCStates.create(id, value);
				currentAutomaton.addState(newGCState);
				currentAutomaton.addEdge(lastGCState, newGCState, GCEdges.createStringEq(optChild.get().getLabel().toString()));
				lastGCState = newGCState;

				if (!nextOptChild.isPresent())
					break;

				optChild = nextOptChild;
			}
		}
		currentAutomaton.setEnd(lastGCState);
		GCStates.setFinal(lastGCState, true);
		GCStates.setTerminal(lastGCState, path.isTerminal());

		// Loop the first state
		if (!path.isRooted())
			currentAutomaton.addEdge(currentAutomaton.getStart(), currentAutomaton.getStart(), GCEdges.createAny());
		if (!path.isTerminal())
			currentAutomaton.addEdge(currentAutomaton.getEnd(), currentAutomaton.getEnd(), GCEdges.createAny());

		return currentAutomaton;
	}

	private GChunk recursiveConstruct(IPRegexElement element)// throws BuilderException
	{
		GChunk currentAutomaton;

		switch (element.getType())
		{
		case KEY:
			Key key = (Key) element;
			currentAutomaton = oneEdge(GCEdges.createStringEq((key.getLabel())));
			break;

		case REGEX:
			Regex regex = (Regex) element;
			currentAutomaton = oneEdge(GCEdges.createRegex(regex.getRegex()));
			break;

		case VALUE:
			@SuppressWarnings("unchecked")
			Value<VAL> c = (Value<VAL>) element;
			currentAutomaton = terminalState(c.getValue());
			break;

		case DISJUNCTION:
			Disjunction orElement = (Disjunction) element;

			List<GChunk> gcs = new ArrayList<>(orElement.getElements().size());

			for (IPRegexElement ie : orElement.getElements())
			{
				GChunk gc = recursiveConstruct(ie);

				if (gc.getNbParentEdges(gc.getStart()) > 0)
					gc.prependEdge(gc.addVertex(), GCEdges.createEpsilon());

				if (gc.getNbChildEdges(gc.getEnd()) > 0)
					gc.appendEdge(gc.addVertex(), GCEdges.createEpsilon());

				gcs.add(gc);
			}
			currentAutomaton = gcs.get(0);

			for (int i = 1, c1 = gcs.size(); i < c1; i++)
				currentAutomaton.glue(gcs.get(i));
			break;

		case SEQUENCE:
			Sequence me = (Sequence) element;

			Iterator<IPRegexElement> iterator = me.getElements().iterator();
			currentAutomaton = recursiveConstruct(iterator.next());

			while (iterator.hasNext())
				currentAutomaton.concat(recursiveConstruct(iterator.next()));
			break;

		default:
			throw new InvalidParameterException("Invalid type for parameter");
		}

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
				currentAutomaton.addEdge(currentAutomaton.getStart(), currentAutomaton.getEnd(), GCEdges.createEpsilon());
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

				currentAutomaton.addEdge(currentAutomaton.getEnd(), currentAutomaton.getStart(), GCEdges.createEpsilon());
			}
			else
			{
				if (sup < inf)
					throw new InvalidParameterException();
				if (sup != inf)
				{
					base.addEdge(base.getStart(), base.getEnd(), GCEdges.createEpsilon());
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
