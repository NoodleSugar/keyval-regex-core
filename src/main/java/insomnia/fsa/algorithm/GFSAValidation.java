package insomnia.fsa.algorithm;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAElement;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IGFSAutomaton;

public class GFSAValidation<VAL, LBL> implements IGFSAValidation<VAL, LBL>
{
	@Override
	public boolean test(IGFSAutomaton<VAL, LBL> automaton, IFSAElement<VAL, LBL> element)
	{
		Collection<IFSAState<VAL, LBL>> states = automaton.getInitialStates();
		states = automaton.nextValidStates(states, element);
		return !Collections.disjoint(states, automaton.getFinalStates());
	}
}
