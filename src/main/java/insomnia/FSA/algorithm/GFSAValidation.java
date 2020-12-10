package insomnia.FSA.algorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import insomnia.FSA.IFSAState;
import insomnia.FSA.IGFSAutomaton;

public class GFSAValidation<E, A extends IGFSAutomaton<E>> implements IFSAAValidation<E, A>
{
	@Override
	public boolean test(A automaton, List<E> elements)
	{
		Collection<IFSAState<E>> states = automaton.getInitialStates();
		states = automaton.nextValidStates(states, elements);
		return !Collections.disjoint(states, automaton.getFinalStates());
	}

}
