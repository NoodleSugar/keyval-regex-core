package insomnia.implem.fsa.fpa.creational;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.regex.parser.IPRegexElement;
import insomnia.implem.data.regex.parser.Quantifier;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fpa.graphchunk.modifier.IGraphChunkModifier;
import insomnia.implem.fsa.fpa.graphchunk.modifier.IGraphChunkModifier.Environment;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;

/**
 * The factory to create an automaton from a parsed regex or a path.
 * 
 * @author zuri
 * @param <V>
 * @param <E>
 */
public class FPAFactory<VAL, LBL>
{
	private GraphChunk<VAL, LBL> automaton, modifiedAutomaton;

	private Function<String, LBL> mapLabel;
	private Function<String, VAL> mapValue;

	public FPAFactory(IPRegexElement elements, Function<String, LBL> mapLabel, Function<String, VAL> mapValue)// throws BuilderException
	{
		this.mapLabel = mapLabel;
		this.mapValue = mapValue;

		automaton         = recursiveConstruct(elements, true);
		modifiedAutomaton = automaton;
	}

	/**
	 * Construct the factory from a path.
	 * 
	 * @param path
	 */
	public FPAFactory(IPath<VAL, LBL> path, Function<String, LBL> mapLabel, Function<String, VAL> mapValue)
	{
		this.mapLabel     = mapLabel;
		this.mapValue     = mapValue;
		automaton         = constructFromPath(path);
		modifiedAutomaton = automaton;
	}

	// =========================================================================

	private void finalizeAutomaton(GraphChunk<VAL, LBL> automaton)
	{
		{
			IFSAState<VAL, LBL> start = automaton.getStart();
			automaton.setInitial(start, true);
		}
		{
			IFSAState<VAL, LBL> end = automaton.getEnd();
			automaton.setFinal(end, true);
		}
	}

	// =========================================================================

	/**
	 * The graph chunk modifier may change the structure of the automaton before the build.
	 * The Consumer may be change between every build.
	 * 
	 * @param graphChunkModifier
	 * @return
	 */
	public FPAFactory<VAL, LBL> setGraphChunkModifier(IGraphChunkModifier<VAL, LBL> graphChunkModifier)
	{
		if (graphChunkModifier == null)
			modifiedAutomaton = automaton;
		else
		{
			Environment<VAL, LBL> env = new Environment<VAL, LBL>()
			{
				@Override
				public GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IFSAState<VAL, LBL> start, IFSAState<VAL, LBL> end, IPath<VAL, LBL> path)
				{
					GraphChunk<VAL, LBL>      ret    = new GraphChunk<>(start, end);
					List<LBL>                 labels = path.getLabels();
					int                       nb     = labels.size();
					List<IFSAState<VAL, LBL>> states = new ArrayList<>(nb + 1);

					states.add(start);

					// Add the states
					for (int i = 0; i < nb - 1; i++)
					{
						IFSAState<VAL, LBL> state = gchunk.createState();
						states.add(state);
					}
					states.add(end);

					// Add the edges
					for (int i = 0; i < nb; i++)
						ret.addEdge(states.get(i), states.get(i + 1), FSALabelConditions.createAnyOrEq(labels.get(i)));

					gchunk.union(ret, gchunk.getStart(), gchunk.getEnd());
					return ret;
				}

				@Override
				public GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IFSAState<VAL, LBL> start, IPath<VAL, LBL> path)
				{
					IFSAState<VAL, LBL> end = gchunk.createState(path.getLeaf().getValue());
					gchunk.setFinal(end, true);
					gchunk.setTerminal(end, path.isTerminal());

					if (!path.isTerminal())
						gchunk.addEdge(end, end, FSALabelConditions.trueCondition());

					return gluePath(gchunk, start, end, path);
				}
			};
			modifiedAutomaton = automaton.copyClone();
			graphChunkModifier.accept(modifiedAutomaton, env);
		}
		return this;
	}

	public IGFPA<VAL, LBL> create()
	{
		return modifiedAutomaton.copyClone();
	}

	public FPABuilder<VAL, LBL> createBuilder()
	{
		return new FPABuilder<>(modifiedAutomaton);
	}

	private GraphChunk<VAL, LBL> constructFromPath(IPath<VAL, LBL> path)
	{
		GraphChunk<VAL, LBL> currentAutomaton = new GraphChunk<>();

		if (path.isEmpty())
			return currentAutomaton;

		INode<VAL, LBL>     pNode = path.getRoot();
		IFSAState<VAL, LBL> lastGCState;

		IEdge<VAL, LBL> optChild = path.getChild(pNode);

		{
			IFSAState<VAL, LBL> newGCState = currentAutomaton.createState(pNode.getValue());
			currentAutomaton.setStart(newGCState);
			currentAutomaton.setRooted(newGCState, path.isRooted());
			lastGCState = newGCState;
		}
		if (null != optChild)
		{
			for (;;)
			{
				pNode = optChild.getChild();
				VAL value = pNode.getValue();

				IEdge<VAL, LBL> nextOptChild = path.getChild(pNode);

				IFSAState<VAL, LBL> newGCState = currentAutomaton.createState(value);
				currentAutomaton.addEdge(lastGCState, newGCState, FSALabelConditions.createAnyOrEq(optChild.getLabel()));
				lastGCState = newGCState;

				if (null == nextOptChild)
					break;

				optChild = nextOptChild;
			}
		}
		currentAutomaton.setEnd(lastGCState);
		currentAutomaton.setTerminal(lastGCState, path.isTerminal());

		finalizeAutomaton(currentAutomaton);
		return currentAutomaton;
	}

	private GraphChunk<VAL, LBL> recursiveConstruct(IPRegexElement element, boolean initialElements)// throws BuilderException
	{
		Quantifier           q = element.getQuantifier();
		GraphChunk<VAL, LBL> currentAutomaton;

		switch (element.getType())
		{
		case EMPTY:
		{
			VAL value = mapValue.apply(element.getValue());
			currentAutomaton = GraphChunk.createOneState(element.isRooted(), element.isTerminal(), value);
			break;
		}

		case KEY:
		{
			VAL value = mapValue.apply(element.getValue());
			LBL label = mapLabel.apply(element.getLabel());

			IFSALabelCondition<LBL> lcondition;
			// Regex
			if (element.getLabel() != null && element.getLabelDelimiters().equals("~~"))
				lcondition = FSALabelConditions.createRegex(element.getLabel());
			else
				lcondition = FSALabelConditions.createAnyOrEq(label);

			currentAutomaton = GraphChunk.createOneEdge(false, lcondition, null, value);

			if (element.isTerminal())
				currentAutomaton.setTerminal(currentAutomaton.getEnd(), true);
			break;
		}

		case DISJUNCTION:
		{
			List<GraphChunk<VAL, LBL>> chunks = new ArrayList<>(element.getElements().size());

			for (IPRegexElement ie : element.getElements())
			{
				GraphChunk<VAL, LBL> chunk = recursiveConstruct(ie, false);
				chunks.add(chunk);
			}
			if (initialElements)
			{
				for (GraphChunk<VAL, LBL> gc : chunks)
					finalizeAutomaton(gc);
			}
			currentAutomaton = glueList(chunks);
			applyQuantifier(currentAutomaton, q);
			return currentAutomaton;
		}

		case SEQUENCE:
		{
			Iterator<IPRegexElement> iterator = element.getElements().iterator();

			if (!iterator.hasNext())
				currentAutomaton = GraphChunk.createOneState(false, false, null);
			else
			{
				currentAutomaton = recursiveConstruct(iterator.next(), false);

				while (iterator.hasNext())
					currentAutomaton.concat(recursiveConstruct(iterator.next(), false));
			}
			break;
		}

		default:
			throw new InvalidParameterException("Invalid type for parameter");
		}
		// One element end
		applyQuantifier(currentAutomaton, q);

		if (initialElements)
			finalizeAutomaton(currentAutomaton);
		return currentAutomaton;
	}

	private GraphChunk<VAL, LBL> glueList(List<GraphChunk<VAL, LBL>> aList)
	{
		if (aList.size() == 1)
			return aList.get(0);

		GraphChunk<VAL, LBL> ret   = new GraphChunk<VAL, LBL>();
		IFSAState<VAL, LBL>  start = ret.createState();
		IFSAState<VAL, LBL>  end   = ret.createState();
		ret.setStart(start);
		ret.setEnd(end);

		for (GraphChunk<VAL, LBL> gc : aList)
		{
			ret.addEdge(start, gc.getStart(), FSALabelConditions.epsilonCondition());
			ret.add(gc);
			ret.addEdge(gc.getEnd(), end, FSALabelConditions.epsilonCondition());
		}
		return ret;
	}

	private void applyQuantifier(GraphChunk<VAL, LBL> gc, Quantifier q)
	{
		int inf = q.getInf();
		int sup = q.getSup();

		if (inf != 1 || sup != 1)
		{
			GraphChunk<VAL, LBL> base = gc.copy();

			if (inf == 1)
				;
			else if (inf == 0)
			{
				gc.cleanGraph();
				gc.addEdge(gc.getStart(), gc.getEnd(), FSALabelConditions.epsilonCondition());
			}
			else if (inf > 1)
				gc.concat(base.copy(), inf - 1);
			else
				throw new InvalidParameterException("inf: " + inf);

			// Infty repeat
			if (sup == -1)
			{
				if (inf == 0)
					gc.glue(base);

				gc.addEdge(gc.getEnd(), gc.getStart(), FSALabelConditions.epsilonCondition());
			}
			else
			{
				if (sup < inf)
					throw new InvalidParameterException();
				if (sup != inf)
				{
					base.addEdge(base.getStart(), base.getEnd(), FSALabelConditions.epsilonCondition());
					GraphChunk<VAL, LBL> repeat = base.copy();
					repeat.concat(base.copy(), sup - inf - 1);

					if (inf == 0)
						gc.glue(repeat);
					else
						gc.concat(base, sup - inf);
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return automaton.toString();
	}
}
