package insomnia.implem.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

import insomnia.data.IPath;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IGFPA;
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
	private Queue<LBL>      labels;

	private boolean end        = false;
	private int     labelIndex = 0;

	private Queue<Result<VAL, LBL>>                    currentResults;
	private Collection<IFSAState<VAL, LBL>>            currentStates;
	private Map<IFSAState<VAL, LBL>, Collection<From>> groupsOffset;

	// =========================================================================

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
		this.labels         = new LinkedList<>(element.getLabels());
		this.currentStates  = new ArrayList<>(automaton.getStates().size());
		this.currentStates.addAll(automaton.getInitialStates());

		GFPAOp.initStates(automaton, currentStates, element);

		// Create a group for each initial state
		for (IFSAState<VAL, LBL> state : currentStates)
			addOffset(groupsOffset, state, 0);
	}

	private void setEnd()
	{
		this.end = true;
	}

	private void addOffsets(Map<IFSAState<VAL, LBL>, Collection<From>> groupsOffset, IFSAState<VAL, LBL> state, Collection<From> srcOffsets)
	{
		Collection<From> offsets = groupsOffset.computeIfAbsent(state, s -> new HashSet<>());
		offsets.addAll(srcOffsets);
	}

	private void addOffset(Map<IFSAState<VAL, LBL>, Collection<From>> groupsOffset, IFSAState<VAL, LBL> state, int offset)
	{
		Collection<From> offsets = groupsOffset.computeIfAbsent(state, s -> new HashSet<>());
		offsets.add(new From(automaton.isRooted(state), offset));
	}

	private void addResult(IFSAState<VAL, LBL> state, From from, int lastOffset)
	{
		int start = from.getFrom();
		int end   = lastOffset;

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

	private void checkResults()
	{
		currentResults.removeIf(r -> !GFPAOp.checkFinalState(automaton, r.finalState));
	}

	private void finalizeResults()
	{
		GFPAOp.finalizeStates(automaton, currentStates, element);
		Queue<Result<VAL, LBL>> ret = new LinkedList<>();

		for (Result<VAL, LBL> result : currentResults)
		{
			if (!currentStates.contains(result.finalState))
				continue;

			if (automaton.isTerminal(result.finalState))
				result.limits = Pair.of(result.limits.getLeft(), result.limits.getRight() + 1);

			ret.add(result);
		}
		this.currentResults = ret;
	}

	/**
	 * @return the next match if exists or {@link TreeMatchResults#empty()}
	 */
	public ITreeMatchResult<VAL, LBL> nextMatch()
	{
		if (end)
			return TreeMatchResults.empty();

		while (!end && currentResults.isEmpty())
			nextValidStep();

		if (currentResults.isEmpty())
			return TreeMatchResults.empty();

		Result<VAL, LBL> result = currentResults.poll();
		return TreeMatchResults.create(element.subPath(result.limits.getLeft(), result.limits.getRight()));
	}

	private void nextValidStep()
	{
		boolean hasMatch = false;

		Map<IFSAState<VAL, LBL>, Collection<From>> nextGroupsOffsets = new HashMap<>();

		while (true)
		{
			LBL label = labels.poll();
			labelIndex++;

			if (currentStates.isEmpty())
				break;

			Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(currentStates);
			currentStates.clear();

			for (IFSAState<VAL, LBL> currentState : buffStates)
			{
				Collection<From> groupOffsets = groupsOffset.getOrDefault(currentState, Collections.emptyList());

				if (groupOffsets.isEmpty())
					continue;

				for (IFSAEdge<VAL, LBL> edge : automaton.getReachableEdges(currentState))
				{
					if (!edge.getLabelCondition().test(label))
						continue;

					IFSAState<VAL, LBL> nextState         = edge.getChild();
					boolean             isInitial         = automaton.isInitial(nextState);
					boolean             isFinal           = automaton.isFinal(nextState);
					boolean             isTerminal        = automaton.isTerminal(nextState);
					int                 nextState_nbEdges = automaton.getEdgesOf(nextState).size();

					if (isInitial && !GFPAOp.checkInitialState(automaton, nextState))
						continue;

					currentStates.add(nextState);

					if (!isInitial //
						&& (!isFinal || (false //
							|| (isTerminal && nextState_nbEdges > 0) //
							|| (!isTerminal && nextState_nbEdges > 1) //
						)) //
					)
						addOffsets(nextGroupsOffsets, nextState, groupOffsets);
					if (isInitial)
						addOffset(nextGroupsOffsets, nextState, labelIndex);
					if (isFinal)
					{
						for (From from : groupOffsets)
							addResult(nextState, from, labelIndex);

						hasMatch = true;
					}
				}
			}
			{
				Map<IFSAState<VAL, LBL>, Collection<From>> tmp = groupsOffset;
				groupsOffset      = nextGroupsOffsets;
				nextGroupsOffsets = tmp;
				nextGroupsOffsets.clear();
			}
			if (labels.isEmpty())
			{
				finalizeResults();
				break;
			}
			else if (hasMatch)
			{
				checkResults();
				return;
			}
		}
		setEnd();
	}
}