package insomnia.fsa.algorithm;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAState;
import insomnia.fsa.IGFSAutomaton;

public class GFSAValidation<E, ELMNT> implements IGFSAValidation<E, ELMNT>
{
	@Override
	public boolean test(IGFSAutomaton<E, ELMNT> automaton, ELMNT element)
	{
		Collection<IFSAState<E>> states = automaton.getInitialStates();
		states = automaton.nextValidStates(states, element);
		return !Collections.disjoint(states, automaton.getFinalStates());
	}
}
