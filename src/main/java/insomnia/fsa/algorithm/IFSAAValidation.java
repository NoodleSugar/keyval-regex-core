package insomnia.fsa.algorithm;

import java.util.function.BiPredicate;

import insomnia.fsa.IFSAutomaton;

public interface IFSAAValidation<ELMNT, A extends IFSAutomaton<ELMNT>> //
	extends BiPredicate<A, ELMNT>
{
}
