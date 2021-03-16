package insomnia.implem.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatchResult;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.regex.PathMatchResults;
import insomnia.implem.data.regex.TreeMatchResults;

/**
 * A class to compute the matching groups of a {@link IPath} element in an {@link IGFPA} processing.
 *
 * @author zuri
 */
class GFPAGroupMatcher<VAL, LBL>
{
	// =========================================================================

	private static class Result<VAL, LBL>
	{
		IFSAState<VAL, LBL>    finalState;
		Pair<Integer, Integer> limits;

		public Result(IFSAState<VAL, LBL> state, Pair<Integer, Integer> limits)
		{
			this.finalState = state;
			this.limits     = limits;
		}

		@Override
		public String toString()
		{
			return new StringBuilder().append(finalState).append(", ").append(limits).toString();
		}
	}

	// =========================================================================

	private IGFPA<VAL, LBL> automaton;
	private IPath<VAL, LBL> element;
	private Iterator<LBL>   labels;
	private Iterator<VAL>   values;

	private int labelIndex = 0;

	private Queue<Result<VAL, LBL>>                    currentResults;
	private Map<IFSAState<VAL, LBL>, Collection<From>> groupsOffset;

	boolean hasNonRootedInitials;

	// =========================================================================

	/**
	 * Create a matcher for an automaton and an element to test.
	 * 
	 * @param <VAL>     type of node value
	 * @param <LBL>     type of edge label
	 * @param automaton the automaton
	 * @param element   the element to search in for groups
	 * @return a group matcher for {@code element} using {@code automaton}
	 */
	public static <VAL, LBL> GFPAGroupMatcher<VAL, LBL> create(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		return new GFPAGroupMatcher<>(automaton, element);
	}

	// =========================================================================

	private GFPAGroupMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		this.automaton = automaton;
		this.element   = element;

		this.currentResults = new LinkedList<>();
		this.groupsOffset   = new HashMap<>();
		this.labels         = element.getLabels().iterator();
		this.values         = element.getValues().iterator();

		hasNonRootedInitials = automaton.getInitialStates().stream().anyMatch(s -> !automaton.isRooted(s));
		values.next();

		// Create a group for each initial state
		for (IFSAState<VAL, LBL> state : IGFPA.getInitials(automaton, element))
			addOffset(groupsOffset, state, 0);

		finalStatesCheck();
	}

	// ==========================================================================

	private Collection<IFSAState<VAL, LBL>> currentStates()
	{
		return groupsOffset.keySet();
	}

	private void addOffsets(Map<IFSAState<VAL, LBL>, Collection<From>> groupsOffset, IFSAState<VAL, LBL> state, Collection<From> srcOffsets)
	{
		Collection<From> offsets = groupsOffset.computeIfAbsent(state, s -> new HashSet<>());
		offsets.addAll(srcOffsets);
	}

	/**
	 * Add an offset for a state.
	 */
	private void addOffset(Map<IFSAState<VAL, LBL>, Collection<From>> groupsOffset, IFSAState<VAL, LBL> state, int offset)
	{
		Collection<From> offsets = groupsOffset.computeIfAbsent(state, s -> new HashSet<>());
		offsets.add(new From(automaton.isRooted(state), offset));
	}

	/**
	 * Check if there is some final states in this.currentStates and add results if needed
	 */
	private void finalStatesCheck()
	{
		// Add new results
		for (IFSAState<VAL, LBL> pfinal : (Iterable<IFSAState<VAL, LBL>>) currentStates().stream().filter(automaton::isFinal)::iterator)
		{
			for (From from : groupsOffset.getOrDefault(pfinal, Collections.emptyList()))
				addResult(pfinal, from, labelIndex);
		}
	}

	private void addResult(IFSAState<VAL, LBL> state, From from, int lastOffset)
	{
		int start = from.getFrom();
		int end   = lastOffset;

		if (automaton.isTerminal(state) && values.hasNext())
			return;
		// Last automaton process: check element terminal case
		if (!values.hasNext())
		{
			if (automaton.isTerminal(state) && !element.isTerminal())
				return;
		}
		if (!from.isRooted())
		{
			if (element.isRooted())
			{
				start++;
				end++;
			}
		}
		else if (element.isRooted())
			end++;

		currentResults.add(new Result<>(state, Pair.of(start, end)));
	}

	private boolean ended()
	{
		return !labels.hasNext() || !mayContinue();
	}

	private boolean mayContinue()
	{
		return hasNonRootedInitials || !currentStates().isEmpty();
	}

	// ==========================================================================

	private IPathMatchResult<VAL, LBL> nextResult()
	{
		Result<VAL, LBL> result = currentResults.poll();
		return PathMatchResults.create(element.subPath(result.limits.getLeft(), result.limits.getRight()));
	}

	/**
	 * @return the next match if exists or {@link TreeMatchResults#empty()}
	 */
	public IPathMatchResult<VAL, LBL> nextMatch()
	{
		if (!currentResults.isEmpty())
			return nextResult();

		if (ended())
			return PathMatchResults.empty();

		nextValidStep();

		if (currentResults.isEmpty())
			return PathMatchResults.empty();
		else
			return nextResult();
	}

	private void nextValidStep()
	{
		while (labels.hasNext())
		{
			LBL label = labels.next();
			VAL value = values.next();
			labelIndex++;

			Collection<IFSAState<VAL, LBL>>                buffStates       = new ArrayList<>(groupsOffset.keySet());
			HashMap<IFSAState<VAL, LBL>, Collection<From>> buffGroupsOffset = new HashMap<>(groupsOffset);
			groupsOffset.clear();

			for (IFSAState<VAL, LBL> currentState : buffStates)
			{
				Collection<From> groupOffsets = buffGroupsOffset.get(currentState);

				if (groupOffsets.isEmpty())
					continue;

				for (IFSAEdge<VAL, LBL> edge : automaton.getReachableEdges(currentState))
				{
					if (!IGFPA.testLabel(edge.getLabelCondition(), label))
						continue;

					IFSAState<VAL, LBL> nextState = edge.getChild();

					if (!IGFPA.testValue(nextState.getValueCondition(), value))
						continue;

					for (IFSAState<VAL, LBL> s : automaton.getEpsilonClosure(nextState))
					{
						addOffsets(groupsOffset, s, groupOffsets);

						if (automaton.isInitial(s))
							addOffset(groupsOffset, s, labelIndex);
					}
				}
			}
			{	// Add new initials
				Collection<IFSAState<VAL, LBL>> initials = IGFPA.internalGetInitials(automaton, value);

				for (IFSAState<VAL, LBL> initial : initials)
					addOffset(groupsOffset, initial, labelIndex);
			}
			finalStatesCheck();

			if (!currentResults.isEmpty() || !mayContinue())
				return;
		}
	}
}