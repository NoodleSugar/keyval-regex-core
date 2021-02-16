package insomnia.implem.fsa.fpa.creational;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.fsa.IFSAState;
import insomnia.fsa.creational.IFSALabelFactory;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fpa.graphchunk.IGCAFactory;
import insomnia.implem.fsa.fpa.graphchunk.modifier.IGraphChunkModifier;
import insomnia.implem.fsa.fpa.graphchunk.modifier.IGraphChunkModifier.Environment;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.kv.pregex.IPRegexElement;
import insomnia.implem.kv.pregex.PRegexElements.Disjunction;
import insomnia.implem.kv.pregex.PRegexElements.Key;
import insomnia.implem.kv.pregex.PRegexElements.Regex;
import insomnia.implem.kv.pregex.PRegexElements.Sequence;
import insomnia.implem.kv.pregex.PRegexElements.Value;
import insomnia.implem.kv.pregex.Quantifier;

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

	public FPAFactory(IPRegexElement elements, IFSALabelFactory<LBL> labelFactory)// throws BuilderException
	{
		automaton         = recursiveConstruct(elements, labelFactory);
		modifiedAutomaton = automaton;

		// Not set in recursiveConstruct()
		IGCAFactory<VAL, LBL> afactory = automaton.getAFactory();
		afactory.setInitial(automaton.getStart(), true);
		afactory.setFinal(automaton.getEnd(), true);
	}

	/**
	 * Construct the factory from a path.
	 * 
	 * @param path
	 */
	public FPAFactory(IPath<VAL, LBL> path)
	{
		automaton         = constructFromPath(path);
		modifiedAutomaton = automaton;
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
						IFSAState<VAL, LBL> state = gchunk.getAFactory().create();
						states.add(state);
					}
					states.add(end);

					// Add the edges
					for (int i = 0; i < nb; i++)
						ret.addEdge(states.get(i), states.get(i + 1), FSALabelConditions.createEq(labels.get(i)));

					gchunk.union(ret, gchunk.getStart(), gchunk.getEnd());
					return ret;
				}

				@Override
				public GraphChunk<VAL, LBL> gluePath(GraphChunk<VAL, LBL> gchunk, IFSAState<VAL, LBL> start, IPath<VAL, LBL> path)
				{
					IGCAFactory<VAL, LBL> afactory = gchunk.getAFactory();
					IFSAState<VAL, LBL>   end      = afactory.create(path.getLeaf().getValue());

					afactory.setFinal(end, true);
					afactory.setTerminal(end, path.isTerminal());

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
		GraphChunk<VAL, LBL>  currentAutomaton = new GraphChunk<>();
		IGCAFactory<VAL, LBL> afactory         = currentAutomaton.getAFactory();

		if (path.isEmpty())
			return currentAutomaton;

		INode<VAL, LBL>     pNode = path.getRoot();
		IFSAState<VAL, LBL> lastGCState;

		IEdge<VAL, LBL> optChild = path.getChild(pNode);

		{
			IFSAState<VAL, LBL> newGCState = afactory.create(pNode.getValue());
			afactory.setRooted(newGCState, path.isRooted());
			afactory.setInitial(newGCState, true);

			currentAutomaton.setStart(newGCState);
			lastGCState = newGCState;
		}

		if (null != optChild)
		{
			for (;;)
			{
				pNode = optChild.getChild();
				VAL value = pNode.getValue();

				IEdge<VAL, LBL> nextOptChild = path.getChild(pNode);

				IFSAState<VAL, LBL> newGCState = afactory.create(value);
				currentAutomaton.addEdge(lastGCState, newGCState, FSALabelConditions.createEq(optChild.getLabel()));
				lastGCState = newGCState;

				if (null == nextOptChild)
					break;

				optChild = nextOptChild;
			}
		}
		currentAutomaton.setEnd(lastGCState);
		afactory.setFinal(lastGCState, true);
		afactory.setTerminal(lastGCState, path.isTerminal());

		// Loop the first state
		if (!path.isRooted())
			currentAutomaton.addEdge(currentAutomaton.getStart(), currentAutomaton.getStart(), FSALabelConditions.trueCondition());
		if (!path.isTerminal())
			currentAutomaton.addEdge(currentAutomaton.getEnd(), currentAutomaton.getEnd(), FSALabelConditions.trueCondition());

		return currentAutomaton;
	}

	private GraphChunk<VAL, LBL> recursiveConstruct(IPRegexElement element, IFSALabelFactory<LBL> labelFactory)// throws BuilderException
	{
		GraphChunk<VAL, LBL> currentAutomaton;

		switch (element.getType())
		{
		case KEY:
			Key key = (Key) element;
			currentAutomaton = GraphChunk.createOneEdge(FSALabelConditions.createEq(labelFactory.create(key.getLabel())));
			break;

		case REGEX:
			Regex regex = (Regex) element;
			currentAutomaton = GraphChunk.createOneEdge(FSALabelConditions.createRegex(regex.getRegex()));
			break;

		case VALUE:
			@SuppressWarnings("unchecked")
			Value<VAL> c = (Value<VAL>) element;
			currentAutomaton = GraphChunk.createOneTerminalState(c.getValue());
			break;

		case DISJUNCTION:
			Disjunction orElement = (Disjunction) element;

			List<GraphChunk<VAL, LBL>> gcs = new ArrayList<>(orElement.getElements().size());

			for (IPRegexElement ie : orElement.getElements())
			{
				GraphChunk<VAL, LBL> gc = recursiveConstruct(ie, labelFactory);

				if (gc.nbParentEdges(gc.getStart()) > 0)
					gc.prependEdge(gc.getAFactory().create(), FSALabelConditions.epsilonCondition());

				if (gc.nbEdges(gc.getEnd()) > 0)
					gc.appendEdge(gc.getAFactory().create(), FSALabelConditions.epsilonCondition());

				gcs.add(gc);
			}
			currentAutomaton = gcs.get(0);

			for (int i = 1, c1 = gcs.size(); i < c1; i++)
				currentAutomaton.glue(gcs.get(i));
			break;

		case SEQUENCE:
			Sequence me = (Sequence) element;

			Iterator<IPRegexElement> iterator = me.getElements().iterator();
			currentAutomaton = recursiveConstruct(iterator.next(), labelFactory);

			while (iterator.hasNext())
				currentAutomaton.concat(recursiveConstruct(iterator.next(), labelFactory));
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
			GraphChunk<VAL, LBL> base = currentAutomaton.copy();

			if (inf == 1)
				;
			else if (inf == 0)
			{
				currentAutomaton.cleanGraph();
				currentAutomaton.addEdge(currentAutomaton.getStart(), currentAutomaton.getEnd(), FSALabelConditions.epsilonCondition());
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

				currentAutomaton.addEdge(currentAutomaton.getEnd(), currentAutomaton.getStart(), FSALabelConditions.epsilonCondition());
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
