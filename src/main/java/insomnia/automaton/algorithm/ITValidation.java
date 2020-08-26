package insomnia.automaton.algorithm;

import java.util.function.BiPredicate;

import insomnia.automaton.ITAutomaton;
import insomnia.rule.tree.ITree;

public interface ITValidation<E> extends BiPredicate<ITAutomaton<E>, ITree<E>>
{

}
