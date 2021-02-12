package insomnia.implem.fsa.fpa;

import java.util.Collections;

import insomnia.data.IPath;
import insomnia.data.regex.AbstractTreeMatcher;
import insomnia.data.regex.ITreeMatcher;
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

	private static class GFPAMatcher<VAL, LBL> //
		extends AbstractTreeMatcher<VAL, LBL> //
		implements ITreeMatcher<VAL, LBL>
	{
		private IGFPA<VAL, LBL> automaton;
		private IPath<VAL, LBL> element;

		private GFPAGroupMatcher<VAL, LBL> groupMatcher;

		public GFPAMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
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

			return matches = !Collections.disjoint(GFPAOp.getNextValidStates(automaton, element), automaton.getFinalStates());
		}

		@Override
		public boolean find()
		{
			return (!TreeMatchResults.empty().equals(matchResult = groupMatcher.nextMatch()));
		}
	}

	// =========================================================================

	public static <VAL, LBL> ITreeMatcher<VAL, LBL> create(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		return new GFPAMatcher<>(automaton, element);
	}
}
