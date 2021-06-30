package insomnia.implem.fsa.fta.edgeCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.lib.help.HelpLists;

final class FTAInclusiveCondition<VAL, LBL> extends FTAAbstractCondition<VAL, LBL>
{
	public FTAInclusiveCondition(IBUFTA<VAL, LBL> automaton, IFTAEdge<VAL, LBL> ftaEdge)
	{
		super(automaton, ftaEdge);
	}

	// ==========================================================================

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
		if (deletedPos.isEmpty())
			return;

		for (List<IFSAState<VAL, LBL>> r : ret)
		{
			for (int i : deletedPos)
				r.add(i, null);
		}
	}

	// ==========================================================================

	@Override
	public Collection<List<IFSAState<VAL, LBL>>> validStates(List<IFSAState<VAL, LBL>> states)
	{
		return validStatesND(states.stream().map(s -> Collections.singleton(s)).collect(Collectors.toList()));
	}

	private Collection<List<IFSAState<VAL, LBL>>> p_validStates(List<IFSAState<VAL, LBL>> states)
	{
		Collection<List<IFSAState<VAL, LBL>>> ret = new ArrayList<>();

		int initialStatesSize = states.size();
		int parentSize        = getParentStates().size();

		if (!test_solve(states))
			return Collections.emptyList();

		Iterable<List<Integer>> indexesIterable = () -> HelpLists.ipowerSetAsStream(states).filter(s -> s.size() == parentSize).iterator();

		for (List<Integer> indexes : indexesIterable)
		{
			List<IFSAState<VAL, LBL>> validStates = new ArrayList<>(initialStatesSize);

			for (int i = 0; i < initialStatesSize; i++)
				validStates.add(indexes.contains(i) ? states.get(i) : null);

			ret.add(validStates);
		}
		return ret;
	}

	@Override
	public Collection<List<IFSAState<VAL, LBL>>> validStatesND(List<? extends Collection<IFSAState<VAL, LBL>>> multiStates)
	{
		List<Collection<IFSAState<VAL, LBL>>> multiStatesBuffer = new ArrayList<>(multiStates);

		int           parentSize = parentStates.size();
		List<Integer> deletedPos = orderedDeleteBadStates(multiStatesBuffer);

		if (multiStatesBuffer.size() < parentSize)
			return Collections.emptyList();

		Collection<List<IFSAState<VAL, LBL>>> ret = new ArrayList<>();

		for (List<IFSAState<VAL, LBL>> states : HelpLists.cartesianProductIterable(multiStatesBuffer))
			ret.addAll(p_validStates(states));
		if (ret.isEmpty())
			return Collections.emptyList();

		fillDeletedPositions(ret, deletedPos);
		return ret;
	}

	// ==========================================================================

	@Override
	public boolean test(List<IFSAState<VAL, LBL>> states)
	{
		if (states.size() < parentStates.size())
			return false;

		return test_solve(CollectionUtils.select(states, s -> parentStates.contains(s), new ArrayList<>()));
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
		for (List<IFSAState<VAL, LBL>> states : HelpLists.cartesianProductIterable(multiStates))
		{
			if (test_solve(states))
				return true;
		}
		return false;
	}

	// ==========================================================================

	@Override
	public String toString()
	{
		return "∀";
	}
}
