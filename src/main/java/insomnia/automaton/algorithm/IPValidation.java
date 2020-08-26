package insomnia.automaton.algorithm;

import java.util.List;
import java.util.function.BiPredicate;

import insomnia.automaton.IPAutomaton;

public interface IPValidation<E> extends BiPredicate<IPAutomaton<E>, List<E>>
{
}
