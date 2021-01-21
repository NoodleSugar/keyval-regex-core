package insomnia.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

/**
 * Bottom-up tree Automaton.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public final class BUFTAOp
{
	private BUFTAOp()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class FTAValidation<VAL, LBL>
	{
		IBUFTA<VAL, LBL> automaton;
		ITree<VAL, LBL>  element;

		public FTAValidation(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
		{
			this.automaton = automaton;
			this.element   = element;
		}

		public Collection<IFSAState<VAL, LBL>> nextValidStates()
		{
			// TODO filter rooted case
			Collection<IFSAState<VAL, LBL>> states = nextValidStates(element.getRoot());

			if (!element.getRoot().isRooted())
				states = states.stream().filter(state -> !automaton.isRooted(state)).collect(Collectors.toList());

			return states;
		}

		public Collection<IFSAState<VAL, LBL>> nextValidStates(INode<VAL, LBL> node)
		{
			List<IEdge<VAL, LBL>> childs = element.getChildren(node);

			// node is a leaf
			if (childs.isEmpty())
			{
				Stream<IFSAState<VAL, LBL>> stream = automaton.getInitialStates().stream();

				if (!node.isTerminal())
					stream = stream.filter(state -> !automaton.isTerminal(state));

				stream = stream.filter(state -> state.getValueCondition().test(node.getValue().orElse(null)));
				return stream.collect(Collectors.toList());
			}
			// A multi childs node
			List<Collection<IFSAState<VAL, LBL>>> lstates = new ArrayList<>(childs.size());

			for (IEdge<VAL, LBL> edge : childs)
			{
				Collection<IFSAState<VAL, LBL>> bottomStates = nextValidStates(edge.getChild());
				Collection<IFSAState<VAL, LBL>> newStates    = new HashSet<>();
				Collection<IFSAEdge<VAL, LBL>>  fsaEdges     = automaton.getEdges(bottomStates);

				// Check the label
				for (IFSAEdge<VAL, LBL> fsaEdge : fsaEdges)
				{
					if (fsaEdge.getLabelCondition().test(edge.getLabel()))
						newStates.add(fsaEdge.getChild());
				}
				lstates.add(newStates);
			}

			/*
			 * If one child node: nothing more to do by construction because no hyper edge exists.
			 */
			if (childs.size() == 1)
				return lstates.isEmpty() ? Collections.emptyList() : lstates.get(0);

			/*
			 * Check hyper transitions
			 */
			Collection<IFTAEdge<VAL, LBL>>  hEdges = automaton.getHyperEdges(lstates);
			Collection<IFSAState<VAL, LBL>> ret    = new HashSet<>();

			for (IFTAEdge<VAL, LBL> hEdge : hEdges)
			{
				if (hEdge.getCondition().testND(lstates))
					ret.add(hEdge.getChild());
			}
			return ret;
		}
	}

	// =========================================================================

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> nextValidStates(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return new FTAValidation<>(automaton, element).nextValidStates();
	}

	public static <VAL, LBL> boolean test(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		Collection<IFSAState<VAL, LBL>> states;// = automaton.getInitialStates();
		states = nextValidStates(automaton, element);
		return !Collections.disjoint(states, automaton.getFinalStates());
	}
}
