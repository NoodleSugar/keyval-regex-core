package insomnia.implem.fsa.fpa;

import java.util.Collections;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatchResult;
import insomnia.data.regex.IPathMatcher;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.regex.PathMatchResults;
import insomnia.implem.data.regex.TreeMatchResults;

public class GFPAMatchers
{
	private GFPAMatchers()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class GFPAMatcher<VAL, LBL> implements IPathMatcher<VAL, LBL>
	{
		private IGFPA<VAL, LBL> automaton;
		private IPath<VAL, LBL> element;

		private GFPAGroupMatcher<VAL, LBL> groupMatcher;

		private IPathMatchResult<VAL, LBL> matchResult;

		public GFPAMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
		{
			this.matchResult  = PathMatchResults.empty();
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

		@Override
		public IPathMatchResult<VAL, LBL> toMatchResult()
		{
			return matchResult;
		}
	}

	// =========================================================================

	public static <VAL, LBL> IPathMatcher<VAL, LBL> create(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		return new GFPAMatcher<>(automaton, element);
	}
}
