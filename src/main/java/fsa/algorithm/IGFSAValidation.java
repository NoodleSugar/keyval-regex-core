package fsa.algorithm;

import java.util.List;

import fsa.IGFSAutomaton;

public interface IGFSAValidation<E> extends IFSAAValidation<E, IGFSAutomaton<E>>
{
	@Override
	public boolean test(IGFSAutomaton<E> automaton, List<E> elements);
}
