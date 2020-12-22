package insomnia.fsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractGFSAutomaton<E, ELMNT> implements IGFSAutomaton<E, ELMNT>
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
	public Collection<IFSAEdge<E>> getEdges(IFSAState<E> state)
	{
		return getEdges(Collections.singletonList(state));
	}

	protected Collection<IFSAState<E>> nextValidState_sync(Collection<? extends IFSAState<E>> states, ELMNT theElement)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<IFSAState<E>>        ret        = new HashSet<>(nbStates());
		Collection<IFSAState<E>> buffStates = new ArrayList<>(nbStates());
		;

		ret.addAll(states);

		for (E element : getLabelsOf(theElement))
		{
			if (ret.isEmpty())
				return Collections.emptyList();

			buffStates.addAll(ret);
			ret.clear();

			for (IFSAEdge<E> edge : getEdges(buffStates))
			{
				if (edge.getLabel().test(element))
					ret.add(edge.getChild());
			}
			buffStates.clear();
		}
		return new ArrayList<>(ret);
	}

	protected Collection<IFSAState<E>> nextValidStates_general(Collection<? extends IFSAState<E>> states, ELMNT theElement)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<IFSAState<E>>        ret = new HashSet<>(nbStates());
		Collection<IFSAState<E>> buffStates;

		ret.addAll(states);

		for (E element : getLabelsOf(theElement))
		{
			if (ret.isEmpty())
				return Collections.emptyList();

			buffStates = epsilonClosure(ret);
			ret.clear();

			for (IFSAEdge<E> edge : getEdges(buffStates))
			{
				if (edge.getLabel().test(element))
					ret.add(edge.getChild());
			}
		}
		return epsilonClosure(new ArrayList<>(ret));
	}

	@Override
	public Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, ELMNT element)
	{
		return nextValidStates(states, element);
	}

	@Override
	public Collection<IFSAState<E>> nextValidStates(IFSAState<E> state, ELMNT element)
	{
		return nextValidStates(Collections.singletonList(state), element);
	}

	@Override
	public Collection<IFSAState<E>> epsilonClosure(Collection<? extends IFSAState<E>> states)
	{
		if (states.isEmpty())
			return Collections.emptyList();

		Set<IFSAState<E>>  ret         = new HashSet<>(nbStates());
		List<IFSAState<E>> buffStates  = new ArrayList<>(nbStates());
		List<IFSAState<E>> addedStates = new ArrayList<>(nbStates());

		ret.addAll(states);
		buffStates.addAll(states);

		while (!buffStates.isEmpty())
		{
			for (IFSAEdge<E> edge : getEdges(buffStates))
			{
				if (edge.getLabel().test() && !ret.contains(edge.getChild()))
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
	public Collection<IFSAState<E>> epsilonClosure(IFSAState<E> state)
	{
		return epsilonClosure(Collections.singletonList(state));
	}

}
