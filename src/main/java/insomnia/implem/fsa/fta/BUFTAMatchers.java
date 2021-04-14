package insomnia.implem.fsa.fta;

import java.util.Collection;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
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
		private ITreeMatchResult<VAL, LBL>  matchResult;

		Matcher(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
		{
			this.matchResult  = TreeMatchResults.empty();
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
			return (!TreeMatchResults.empty().equals(matchResult = groupMatcher.nextMatch()));
		}

		@Override
		public ITreeMatchResult<VAL, LBL> toMatchResult()
		{
			return matchResult;
		}
	}

	// =========================================================================

	public static <VAL, LBL> ITreeMatcher<VAL, LBL> create(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return new Matcher<>(automaton, element);
	}

	/**
	 * Read an element and get the states assigned to the root before the last {@link IFTAEdge} check.
	 * 
	 * @param automaton the automaton
	 * @param element   the element
	 * @return a {@link Collection} of all {@link IFSAState} assigned to the root of {@code element} before the last {@link IFTAEdge} check.
	 */
	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getPreFTAMatchingStates(IBUFTA<VAL, LBL> automaton, ITree<VAL, LBL> element)
	{
		return BUFTAPreStateMatcher.create(automaton, element).matches();
	}
}
