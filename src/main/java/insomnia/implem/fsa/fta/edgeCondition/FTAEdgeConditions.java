package insomnia.implem.fsa.fta.edgeCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.lang3.tuple.Pair;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdgeCondition;
import insomnia.lib.help.HelpLists;

public class FTAEdgeConditions
{
	private static class FTAEdgeCondition<VAL, LBL> implements IFTAEdgeCondition<VAL, LBL>
	{
		private List<IFSAState<VAL, LBL>> parentStates;

		public FTAEdgeCondition(List<IFSAState<VAL, LBL>> states)
		{
			this.parentStates = states;
		}

		/**
		 * Avoid states from {@code multiStates} that are not in {@code this.parentStates}
		 */
		private List<Collection<IFSAState<VAL, LBL>>> cleanBadStates(List<Collection<IFSAState<VAL, LBL>>> multiStates)
		{
			return multiStates = multiStates.stream() //
				.map(states -> CollectionUtils.select(states, s -> parentStates.contains(s))) //
				.filter(c -> !c.isEmpty()) //
				.collect(Collectors.toList());
		}

		/**
		 * @param multiStates_out (out value) the multistates to clean
		 * @return delete positions
		 */
		private List<Integer> orderedDeleteBadStates(List<Collection<IFSAState<VAL, LBL>>> multiStates_out)
		{
			int           msize      = multiStates_out.size();
			List<Integer> deletedPos = new ArrayList<>(msize);

			List<Collection<IFSAState<VAL, LBL>>> tmpMultiStates = multiStates_out.stream() //
				.map(states -> CollectionUtils.select(states, s -> parentStates.contains(s))) //
				.collect(Collectors.toList());
			multiStates_out.clear();

			// Clean multistates but remain all their initial positions
			for (int i = 0; i < msize; i++)
			{
				if (tmpMultiStates.get(i).isEmpty())
					deletedPos.add(i);
				else
					multiStates_out.add(tmpMultiStates.get(i));
			}
			return deletedPos;
		}

		private void fillDeletedPositions(Collection<List<IFSAState<VAL, LBL>>> ret, List<Integer> deletedPos)
		{
			for (List<IFSAState<VAL, LBL>> r : ret)
			{
				for (int i : deletedPos)
					r.add(i, null);
			}
		}

		@Override
		public Collection<List<IFSAState<VAL, LBL>>> validStates(List<IFSAState<VAL, LBL>> states)
		{
			return validStatesND(states.stream().map(s -> Collections.singleton(s)).collect(Collectors.toList()));
		}

		private Collection<List<IFSAState<VAL, LBL>>> p_validStates(List<IFSAState<VAL, LBL>> states)
		{
			int parentSize        = parentStates.size();
			int initialStatesSize = states.size();

			Collection<List<IFSAState<VAL, LBL>>>         ret            = new ArrayList<>();
			Map<IFSAState<VAL, LBL>, Collection<Integer>> statePositions = validStates_solve(states);

			if (statePositions.isEmpty())
				return Collections.emptyList();

			List<Pair<IFSAState<VAL, LBL>, Collection<Integer>>> entries = new ArrayList<>(statePositions.size());

			for (IFSAState<VAL, LBL> state : states)
				entries.add(Pair.of(state, statePositions.get(state)));

			for (List<Integer> indexes : new IteratorIterable<>(HelpLists.cartesianProduct(CollectionUtils.collect(entries, e -> e.getValue()))))
			{
				List<IFSAState<VAL, LBL>> validStates = new ArrayList<>(parentSize);

				for (int i = 0; i < initialStatesSize; i++)
					validStates.add(indexes.contains(i) ? states.get(i) : null);
				ret.add(validStates);
			}
			return ret;
		}

		@Override
		public Collection<List<IFSAState<VAL, LBL>>> validStatesND(List<Collection<IFSAState<VAL, LBL>>> multiStates)
		{
			int           parentSize = parentStates.size();
			List<Integer> deletedPos = orderedDeleteBadStates(multiStates);

			if (multiStates.size() < parentSize)
				return Collections.emptyList();

			Collection<List<IFSAState<VAL, LBL>>> ret = new ArrayList<>();

			for (List<IFSAState<VAL, LBL>> states : new IteratorIterable<>(HelpLists.cartesianProduct(multiStates)))
				ret.addAll(p_validStates(states));
			if (ret.isEmpty())
				return Collections.emptyList();

			fillDeletedPositions(ret, deletedPos);
			return new ArrayList<>(ret);
		}

		private Map<IFSAState<VAL, LBL>, Collection<Integer>> validStates_solve(List<IFSAState<VAL, LBL>> states)
		{
			if (!test_solve(states))
				return Collections.emptyMap();

			Map<IFSAState<VAL, LBL>, Collection<Integer>> statePositions = new HashMap<>();

			for (IFSAState<VAL, LBL> parentState : parentStates)
				statePositions.put(parentState, new ArrayList<>());

			int i = 0;
			for (IFSAState<VAL, LBL> state : states)
				statePositions.get(state).add(i++);

			return statePositions;
		}

		@Override
		public boolean test(List<IFSAState<VAL, LBL>> states)
		{
			if (states.size() < parentStates.size())
				return false;

			return test_solve(ListUtils.predicatedList(states, s -> parentStates.contains(s)));
		}

		/**
		 * Solve the problem as a naive CSP
		 */
		private boolean test_solve(List<IFSAState<VAL, LBL>> states)
		{
			Bag<IFSAState<VAL, LBL>> variables = new HashBag<>(parentStates);
			Bag<IFSAState<VAL, LBL>> values    = new HashBag<>(states);

			for (IFSAState<VAL, LBL> var : variables)
				if (values.getCount(var) < variables.getCount(var))
					return false;
			return true;
		}

		@Override
		public boolean testND(List<Collection<IFSAState<VAL, LBL>>> multiStates)
		{
			multiStates = cleanBadStates(multiStates);

			if (multiStates.size() < parentStates.size())
				return false;

			// TODO: specific algorithm for not testing all the cases
			for (List<IFSAState<VAL, LBL>> states : new IteratorIterable<>(HelpLists.cartesianProduct(multiStates)))
			{
				if (test_solve(states))
					return true;
			}
			return false;
		}

		@Override
		public String toString()
		{
			return "∀";
		}
	}

	/**
	 * Create a state that must contain at least the 'parents' states.
	 */
	public static <VAL, LBL> IFTAEdgeCondition<VAL, LBL> createInclusive(List<IFSAState<VAL, LBL>> states)
	{
		return new FTAEdgeCondition<>(states);
	}
}
