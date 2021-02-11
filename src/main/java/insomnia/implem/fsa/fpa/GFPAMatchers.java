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
			return GFPAOp.nextValidStates(automaton, element);
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
			return GFPAOp.nextValidStatesSync(automaton, element);
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
