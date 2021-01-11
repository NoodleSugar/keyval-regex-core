package insomnia.fsa.fpa.algorithm;

import java.util.Collection;
import java.util.Collections;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IFPAPath;
import insomnia.fsa.fpa.IGFPA;

public class GFPAValidation<VAL, LBL> implements IGFPAValidation<VAL, LBL>
{
	@Override
	public boolean test(IGFPA<VAL, LBL> automaton, IFPAPath<VAL, LBL> element)
	{
		Collection<IFSAState<VAL, LBL>> states = automaton.getInitialStates();
		states = automaton.nextValidStates(states, element);
		return !Collections.disjoint(states, automaton.getFinalStates());
	}
}
