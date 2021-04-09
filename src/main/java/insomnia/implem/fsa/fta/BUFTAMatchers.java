package insomnia.implem.fsa.fta;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fta.IBUFTA;
import insomnia.implem.data.regex.TreeMatchResults;

public final class BUFTAMatchers
{
	private BUFTAMatchers()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class Matcher<VAL, LBL> implements ITreeMatcher<VAL, LBL>
	{
		private ITree<VAL, LBL>             element;
		private IBUFTA<VAL, LBL>            automaton;
		private BUFTAGroupMatcher<VAL, LBL> groupMatcher;
		private ITreeBothResults<VAL, LBL>  matchResult;

		Matcher(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
		{
			this.matchResult  = TreeMatchResults.emptyBoth();
			this.element      = element;
			this.automaton    = automaton;
			this.groupMatcher = BUFTAGroupMatcher.create(automaton, element);
		}

		private Boolean matches = null;

		@Override
		public boolean matches()
		{
			if (null != matches)
				return matches;

			return matches = BUFTAMatches.create(automaton, element).matches();
		}

		@Override
		public boolean find()
		{
			return (!TreeMatchResults.emptyBoth().equals(matchResult = groupMatcher.nextMatch()));
		}

		@Override
		public ITreeMatchResult<VAL, LBL> toMatchResult()
		{
			return matchResult.standard();
		}

		@Override
		public ITreeMatchResult<VAL, LBL> originalMatchResult()
		{
			return matchResult.original();
		}

		@Override
		public ITreeBothResults<VAL, LBL> bothResults()
		{
			return matchResult;
		}
	}

	// =========================================================================

	public static <VAL, LBL> ITreeMatcher<VAL, LBL> create(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return new Matcher<>(automaton, element);
	}
}
