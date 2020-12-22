package insomnia.fsa.algorithm;

import insomnia.fsa.IGFSAutomaton;

public interface IGFSAValidation<E, ELMNT> extends IFSAAValidation<ELMNT, IGFSAutomaton<E, ELMNT>>
{
	@Override
	public boolean test(IGFSAutomaton<E, ELMNT> automaton, ELMNT element);
}
