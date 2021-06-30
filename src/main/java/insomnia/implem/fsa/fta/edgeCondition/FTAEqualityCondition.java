package insomnia.implem.fsa.fta.edgeCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.iterators.IteratorIterable;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.lib.help.HelpLists;

final class FTAEqualityCondition<VAL, LBL> extends FTAAbstractCondition<VAL, LBL>
{
	public FTAEqualityCondition(IBUFTA<VAL, LBL> automaton, IFTAEdge<VAL, LBL> ftaEdge)
	{
		super(automaton, ftaEdge);
	}

	// ==========================================================================

	/**
	 * Avoid states from {@code multiStates} that are not in {@code this.parentStates}
	 */
	private List<Collection<IFSAState<VAL, LBL>>> cleanBadStates(List<? extends Collection<IFSAState<VAL, LBL>>> multiStates)
	{
		return multiStates.stream() //
			.map(states -> CollectionUtils.select(states, s -> parentStates.contains(s))) //
			.collect(Collectors.toCollection(() -> new ArrayList<Collection<IFSAState<VAL, LBL>>>()));
	}

	private boolean preConditions(List<? extends Collection<IFSAState<VAL, LBL>>> multiStates)
	{
		if (multiStates.size() != parentStates.size())
			return false;

		multiStates = cleanBadStates(multiStates);

		if (IterableUtils.matchesAny(multiStates, ms -> ms.isEmpty()))
			return false;

		return true;
	}

	// ==========================================================================

	@Override
	public Collection<List<IFSAState<VAL, LBL>>> validStates(List<IFSAState<VAL, LBL>> states)
	{
		return validStatesND(CollectionUtils.collect(states, s -> Collections.singleton(s), new ArrayList<>()));
	}

	@Override
	public Collection<List<IFSAState<VAL, LBL>>> validStatesND(List<? extends Collection<IFSAState<VAL, LBL>>> multiStates)
	{
		if (!preConditions(multiStates))
			return Collections.emptyList();

		List<Collection<IFSAState<VAL, LBL>>> multiStatesBuffer = new ArrayList<>(multiStates);
		Collection<List<IFSAState<VAL, LBL>>> ret               = new ArrayList<>();

		for (List<IFSAState<VAL, LBL>> states : new IteratorIterable<>(HelpLists.cartesianProduct(multiStatesBuffer)))
		{
			if (test_solve(states))
				ret.add(states);
		}
		if (ret.isEmpty())
			return Collections.emptyList();

		return new ArrayList<>(ret);
	}

	// ==========================================================================

	@Override
	public boolean test(List<IFSAState<VAL, LBL>> states)
	{
		if (states.size() != parentStates.size())
			return false;

		return test_solve(states);
	}

	/**
	 * Solve the problem as a naive CSP
	 */
	private boolean test_solve(List<IFSAState<VAL, LBL>> states)
	{
		Bag<IFSAState<VAL, LBL>> variables = new HashBag<>(parentStates);
		Bag<IFSAState<VAL, LBL>> values    = new HashBag<>(states);
		return variables.equals(values);
	}

	public boolean testND(List<Collection<IFSAState<VAL, LBL>>> multiStates)
	{
		if (!preConditions(multiStates))
			return false;

		// TODO: specific algorithm for not testing all the cases
		for (List<IFSAState<VAL, LBL>> states : new IteratorIterable<>(HelpLists.cartesianProduct(multiStates)))
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
		return "=";
	}
}
