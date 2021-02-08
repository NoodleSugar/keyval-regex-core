package insomnia.implem.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import insomnia.data.IPath;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IGFPA;

public class GFPAMatchers
{
	private GFPAMatchers()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static <VAL, LBL> void cleanBadStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, IPath<VAL, LBL> theElement)
	{
		if (!theElement.isTerminal())
			states.removeIf(state -> automaton.isTerminal(state));

		states.removeIf(state -> false == state.getValueCondition().test(theElement.getValue().orElse(null)));
	}

	private static <VAL, LBL> boolean checkPreConditions(IGFPA<VAL, LBL> automaton, Collection<? extends IFSAState<VAL, LBL>> states, IPath<VAL, LBL> theElement)
	{
		if (states.isEmpty())
			return false;

		if (!theElement.isRooted())
			states.removeIf((state) -> automaton.isRooted(state));

		return true;
	}

	private abstract static class AbstractGFPAMatcher<VAL, LBL> implements ITreeMatcher<VAL, LBL>
	{
		protected IGFPA<VAL, LBL> automaton;
		protected IPath<VAL, LBL> element;

		public AbstractGFPAMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
		{
			this.automaton = automaton;
			this.element   = element;
		}

		@Override
		public boolean matches()
		{
			return !Collections.disjoint(doMatch(), automaton.getFinalStates());
		}

		abstract Collection<IFSAState<VAL, LBL>> doMatch();
	}

	// =========================================================================

	private static class GFPAMatcher<VAL, LBL> extends AbstractGFPAMatcher<VAL, LBL>
	{
		public GFPAMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
		{
			super(automaton, element);
		}

		Collection<IFSAState<VAL, LBL>> doMatch()
		{
			Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(automaton.nbStates() * 2);
			Collection<IFSAState<VAL, LBL>> buffStates;

			ret.addAll(automaton.getInitialStates());

			if (!checkPreConditions(automaton, ret, element))
				return Collections.emptyList();

			for (LBL element : element.getLabels())
			{
				if (ret.isEmpty())
					return Collections.emptyList();

				buffStates = GFPAOp.epsilonClosure(automaton, ret);
				ret.clear();

				for (IFSAEdge<VAL, LBL> edge : automaton.getEdges(buffStates))
				{
					if (edge.getLabelCondition().test(element))
						ret.add(edge.getChild());
				}
			}
			ret = GFPAOp.epsilonClosure(automaton, ret);
			cleanBadStates(automaton, ret, element);
			return ret;
		}
	}

	private static class GFPAMatcherSync<VAL, LBL> extends AbstractGFPAMatcher<VAL, LBL>
	{
		public GFPAMatcherSync(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
		{
			super(automaton, element);
		}

		Collection<IFSAState<VAL, LBL>> doMatch()
		{
			Set<IFSAState<VAL, LBL>>        ret        = new HashSet<>(automaton.nbStates() * 2);
			Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(automaton.nbStates());

			ret.addAll(automaton.getInitialStates());

			if (!checkPreConditions(automaton, ret, element))
				return Collections.emptyList();

			for (LBL element : element.getLabels())
			{
				if (ret.isEmpty())
					return Collections.emptyList();

				buffStates.addAll(ret);
				ret.clear();

				for (IFSAEdge<VAL, LBL> edge : automaton.getEdges(buffStates))
				{
					if (edge.getLabelCondition().test(element))
						ret.add(edge.getChild());
				}
				buffStates.clear();
			}
			cleanBadStates(automaton, ret, element);
			return new ArrayList<>(ret);
		}
	}

	// =========================================================================

	public static <VAL, LBL> ITreeMatcher<VAL, LBL> create(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		if (automaton.getProperties().isSynchronous())
			return new GFPAMatcherSync<>(automaton, element);

		return new GFPAMatcher<>(automaton, element);
	}
}
