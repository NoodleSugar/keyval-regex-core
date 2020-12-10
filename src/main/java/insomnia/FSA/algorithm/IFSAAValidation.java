package insomnia.FSA.algorithm;

import java.util.List;
import java.util.function.BiPredicate;

import insomnia.FSA.IFSAutomaton;

public interface IFSAAValidation<E, A extends IFSAutomaton<E>> extends BiPredicate<A, List<E>>
{
}
