package insomnia.fsa.algorithm;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAState;
import insomnia.fsa.IGFSAutomaton;

public class GFSAValidation<VAL, LBL, ELMNT> implements IGFSAValidation<VAL, LBL, ELMNT>
{
	@Override
	public boolean test(IGFSAutomaton<VAL, LBL, ELMNT> automaton, ELMNT element)
	{
		Collection<IFSAState<VAL, LBL>> states = automaton.getInitialStates();
		states = automaton.nextValidStates(states, element);
		return !Collections.disjoint(states, automaton.getFinalStates());
	}
}
