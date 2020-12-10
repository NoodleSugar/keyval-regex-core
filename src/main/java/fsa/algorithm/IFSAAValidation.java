package fsa.algorithm;

import java.util.List;
import java.util.function.BiPredicate;

import fsa.IFSAutomaton;

public interface IFSAAValidation<E, A extends IFSAutomaton<E>> extends BiPredicate<A, List<E>>
{
}
