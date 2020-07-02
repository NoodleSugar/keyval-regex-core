package insomnia.automaton.algorithm;

import java.util.List;

import insomnia.automaton.IAutomaton;

public interface IValidation<E>
{
	boolean check(IAutomaton<E> automaton, List<E> elements);
}
