package insomnia.fsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractGFSAutomaton<VAL, LBL, ELMNT> implements IGFSAutomaton<VAL, LBL, ELMNT>
{
	protected <T> Set<T> provideSet()
	{
		return new HashSet<T>();
	}

	protected <T> List<T> provideList()
	{
		return new ArrayList<T>();
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges(IFSAState<VAL, LBL> state)
	{
		return getEdges(Collections.singletonList(state));
	}

	private void cleanBadStates(Collection<IFSAState<VAL, LBL>> states, VAL value)
	{
		states.removeIf(state -> false == state.getValueCondition().test(value));
	}

	protected Collection<IFSAState<VAL, LBL>> nextValidState_sync(Collection<? extends IFSAState<VAL, LBL>> states, ELMNT theElement)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<IFSAState<VAL, LBL>>        ret        = new HashSet<>(nbStates());
		Collection<IFSAState<VAL, LBL>> buffStates = new ArrayList<>(nbStates());

		ret.addAll(states);

		for (LBL element : getLabelsOf(theElement))
		{
			if (ret.isEmpty())
				return Collections.emptyList();

			buffStates.addAll(ret);
			ret.clear();

			for (IFSAEdge<VAL, LBL> edge : getEdges(buffStates))
			{
				if (edge.getLabelCondition().test(element))
					ret.add(edge.getChild());
			}
			buffStates.clear();
		}
		cleanBadStates(ret, getValueOf(theElement));
		return new ArrayList<>(ret);
	}

	protected Collection<IFSAState<VAL, LBL>> nextValidStates_general(Collection<? extends IFSAState<VAL, LBL>> states, ELMNT theElement)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Collection<IFSAState<VAL, LBL>> ret = new HashSet<>(nbStates());
		Collection<IFSAState<VAL, LBL>> buffStates;

		ret.addAll(states);

		for (LBL element : getLabelsOf(theElement))
		{
			if (ret.isEmpty())
				return Collections.emptyList();

			buffStates = epsilonClosure(ret);
			ret.clear();

			for (IFSAEdge<VAL, LBL> edge : getEdges(buffStates))
			{
				if (edge.getLabelCondition().test(element))
					ret.add(edge.getChild());
			}
		}
		ret = epsilonClosure(ret);
		cleanBadStates(ret, getValueOf(theElement));
		return ret;
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, ELMNT element)
	{
		return nextValidStates(states, element);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> nextValidStates(IFSAState<VAL, LBL> state, ELMNT element)
	{
		return nextValidStates(Collections.singletonList(state), element);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> epsilonClosure(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<IFSAState<VAL, LBL>>  ret         = new HashSet<>(nbStates());
		List<IFSAState<VAL, LBL>> buffStates  = new ArrayList<>(nbStates());
		List<IFSAState<VAL, LBL>> addedStates = new ArrayList<>(nbStates());

		ret.addAll(states);
		buffStates.addAll(states);

		while (!buffStates.isEmpty())
		{
			for (IFSAEdge<VAL, LBL> edge : getEdges(buffStates))
			{
				if (edge.getLabelCondition().test() && !ret.contains(edge.getChild()))
					addedStates.add(edge.getChild());
			}
			buffStates.clear();
			buffStates.addAll(addedStates);
			ret.addAll(addedStates);
			addedStates.clear();
		}
		return new ArrayList<>(ret);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> epsilonClosure(IFSAState<VAL, LBL> state)
	{
		return epsilonClosure(Collections.singletonList(state));
	}
}
