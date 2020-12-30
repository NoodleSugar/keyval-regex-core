package insomnia.fsa.algorithm;

import insomnia.fsa.IGFSAutomaton;

public interface IGFSAValidation<VAL, LBL, ELMNT> extends IFSAAValidation<ELMNT, IGFSAutomaton<VAL, LBL, ELMNT>>
{
	@Override
	public boolean test(IGFSAutomaton<VAL, LBL, ELMNT> automaton, ELMNT element);
}
