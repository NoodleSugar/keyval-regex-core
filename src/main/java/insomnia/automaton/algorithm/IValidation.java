package insomnia.automaton.algorithm;

import java.util.List;
import java.util.function.BiPredicate;

import insomnia.automaton.IAutomaton;

public interface IValidation<E> extends BiPredicate<IAutomaton<E>, List<E>>
{
}
