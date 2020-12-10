package fsa.algorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fsa.IFSAState;
import fsa.IGFSAutomaton;

public class GFSAValidation<E> implements IGFSAValidation<E>
{
	@Override
	public boolean test(IGFSAutomaton<E> automaton, List<E> elements)
	{
		Collection<IFSAState<E>> states = automaton.getInitialStates();
		states = automaton.nextValidStates(states, elements);
		return !Collections.disjoint(states, automaton.getFinalStates());
	}
}
