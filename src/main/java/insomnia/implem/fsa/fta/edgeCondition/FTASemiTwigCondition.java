package insomnia.implem.fsa.fta.edgeCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.iterators.IteratorIterable;

import insomnia.fsa.IFSAState;
import insomnia.lib.help.HelpLists;

final class FTASemiTwigCondition<VAL, LBL> extends FTAAbstractCondition<VAL, LBL>
{
	public FTASemiTwigCondition(List<IFSAState<VAL, LBL>> states)
	{
		super(states);
	}

	// ==========================================================================

	private boolean preConditions(List<Collection<IFSAState<VAL, LBL>>> multiStates)
	{
		if (multiStates.size() > parentStates.size())
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
	public Collection<List<IFSAState<VAL, LBL>>> validStatesND(List<Collection<IFSAState<VAL, LBL>>> multiStates)
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
		if (states.size() > parentStates.size())
			return false;

		return test_solve(states);
	}

	private boolean test_solve(List<IFSAState<VAL, LBL>> states)
	{
		Bag<IFSAState<VAL, LBL>> searchIn  = new HashBag<>(parentStates);
		Bag<IFSAState<VAL, LBL>> searchFor = new HashBag<>(states);

		for (IFSAState<VAL, LBL> state : searchFor)
			if (searchIn.getCount(state) < searchFor.getCount(state))
				return false;
		return true;
	}

	@Override
	public boolean testND(List<Collection<IFSAState<VAL, LBL>>> multiStates)
	{
		if (!preConditions(multiStates))
			return false;

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
		return "⧊";
	}
}
