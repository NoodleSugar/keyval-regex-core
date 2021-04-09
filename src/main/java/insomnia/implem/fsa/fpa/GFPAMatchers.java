package insomnia.implem.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.tuple.Pair;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatchResult;
import insomnia.data.regex.IPathMatcher;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.Trees;
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
			this.matchResult = PathMatchResults.empty();
			this.automaton   = automaton;
			this.element     = element;
		}

		private Boolean matches = null;

		@Override
		public boolean matches()
		{
			if (null != matches)
				return matches;

			return matches = !Collections.disjoint(getNextValidStates(automaton, element), automaton.getFinalStates());
		}

		@Override
		public boolean find()
		{
			if (groupMatcher == null)
				groupMatcher = GFPAGroupMatcher.create(automaton, element);

			return (!TreeMatchResults.empty().equals(matchResult = groupMatcher.nextMatch()));
		}

		@Override
		public IPathMatchResult<VAL, LBL> toMatchResult()
		{
			return matchResult;
		}

		@Override
		public ITreeMatchResult<VAL, LBL> originalMatchResult()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ITreeBothResults<VAL, LBL> bothResults()
		{
			return TreeMatchResults.createBoth(matchResult, originalMatchResult());
		}
	}

	// =========================================================================

	private static <VAL, LBL> void nextValidStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> states, LBL label, VAL value)
	{
		Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(states);
		states.clear();

		for (IFSAEdge<VAL, LBL> edge : automaton.getEdgesOf(buffStates))
		{
			if (!IGFPA.testLabel(edge.getLabelCondition(), label))
				continue;

			states.addAll(IGFPA.getValidStates(automaton, edge.getChild(), value));
		}
	}

	static <VAL, LBL> Pair<Iterator<VAL>, Iterator<LBL>> getIterators(IPath<VAL, LBL> element)
	{
		Iterator<LBL> labels = element.getLabels().iterator();
		Iterator<VAL> values = element.getValues().iterator();

		if (element.isRooted())
		{
			values = IteratorUtils.chainedIterator(IteratorUtils.singletonIterator(Trees.getRootValue()), values);
			labels = IteratorUtils.chainedIterator(IteratorUtils.singletonIterator(null), labels);
		}
		if (element.isTerminal())
		{
			values = IteratorUtils.chainedIterator(values, IteratorUtils.singletonIterator(Trees.getTerminalValue()));
			labels = IteratorUtils.chainedIterator(labels, IteratorUtils.singletonIterator(null));
		}
		return Pair.of(values, labels);
	}

	private static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getNextValidStates(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(automaton.getStates().size() * 2);

		var           iterators = getIterators(element);
		Iterator<LBL> labels    = iterators.getRight();
		Iterator<VAL> values    = iterators.getLeft();
		ret.addAll(IGFPA.getInitials(automaton, values.next()));

		while (labels.hasNext() && !ret.isEmpty())
			nextValidStates(automaton, ret, labels.next(), values.next());
		return ret;
	}

	// =========================================================================

	public static <VAL, LBL> IPathMatcher<VAL, LBL> create(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		return new GFPAMatcher<>(automaton, element);
	}
}
