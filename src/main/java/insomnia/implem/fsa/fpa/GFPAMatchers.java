package insomnia.implem.fsa.fpa;

import java.util.Collection;
import java.util.Collections;

import insomnia.data.IPath;
import insomnia.data.regex.AbstractTreeMatcher;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.regex.TreeMatchResults;

public class GFPAMatchers
{
	private GFPAMatchers()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private abstract static class AbstractGFPAMatcher<VAL, LBL> //
		extends AbstractTreeMatcher<VAL, LBL> //
		implements ITreeMatcher<VAL, LBL>
	{
		protected IGFPA<VAL, LBL> automaton;
		protected IPath<VAL, LBL> element;

		private GFPAGroupMatcher<VAL, LBL> groupMatcher;

		abstract Collection<IFSAState<VAL, LBL>> doMatch();

		public AbstractGFPAMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
		{
			super();
			this.automaton    = automaton;
			this.element      = element;
			this.groupMatcher = GFPAGroupMatcher.create(automaton, element);
		}

		private Boolean matches = null;

		@Override
		public boolean matches()
		{
			if (null != matches)
				return matches;

			return matches = !Collections.disjoint(doMatch(), automaton.getFinalStates());
		}

		@Override
		public boolean find()
		{
			return (!TreeMatchResults.empty().equals(matchResult = groupMatcher.nextMatch()));
		}
	}

	// =========================================================================

	private static class GFPAMatcher<VAL, LBL> extends AbstractGFPAMatcher<VAL, LBL>
	{
		public GFPAMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
		{
			super(automaton, element);
		}

		@Override
		Collection<IFSAState<VAL, LBL>> doMatch()
		{
			Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(automaton.nbStates() * 2);
			Collection<IFSAState<VAL, LBL>> buffStates;

			ret.addAll(automaton.getInitialStates());
			GFPAOp.initStates(automaton, ret, element);

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
			GFPAOp.finalizeStates(automaton, ret, element);
			return ret;
		}
	}

	// =========================================================================

	private static class GFPAMatcherSync<VAL, LBL> extends AbstractGFPAMatcher<VAL, LBL>
	{
		public GFPAMatcherSync(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
		{
			super(automaton, element);
		}

		@Override
		Collection<IFSAState<VAL, LBL>> doMatch()
		{
			Set<IFSAState<VAL, LBL>>        ret        = new HashSet<>(automaton.nbStates() * 2);
			Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(automaton.nbStates());

			ret.addAll(automaton.getInitialStates());
			GFPAOp.initStates(automaton, ret, element);

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
			GFPAOp.finalizeStates(automaton, ret, element);
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
