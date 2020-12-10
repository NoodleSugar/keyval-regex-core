package insomnia.automaton.algorithm;

import java.util.List;
import java.util.function.BiPredicate;

import insomnia.automaton.IGAutomaton;

public interface IValidation<E> extends BiPredicate<IGAutomaton<E>, List<E>>
{
}
