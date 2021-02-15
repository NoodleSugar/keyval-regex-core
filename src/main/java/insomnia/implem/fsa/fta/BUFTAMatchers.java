package insomnia.implem.fsa.fta;

import java.util.Collections;

import insomnia.data.ITree;
import insomnia.data.regex.AbstractTreeMatcher;
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

	private static class Matcher<VAL, LBL> extends AbstractTreeMatcher<VAL, LBL> //
		implements ITreeMatcher<VAL, LBL>
	{
		private ITree<VAL, LBL>             element;
		private IBUFTA<VAL, LBL>            automaton;
		private BUFTAGroupMatcher<VAL, LBL> groupMatcher;

		Matcher(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
		{
			super();
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

			return !Collections.disjoint(new BUFTAMatches<>(automaton, element).nextValidStates(), automaton.getGFPA().getFinalStates());
		}

		@Override
		public boolean find()
		{
			return (!TreeMatchResults.empty().equals(matchResult = groupMatcher.nextMatch()));
		}
	}

	// =========================================================================

	public static <VAL, LBL> ITreeMatcher<VAL, LBL> create(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return new Matcher<>(automaton, element);
	}
}
