package insomnia.fsa;

import java.util.function.Predicate;

import insomnia.fsa.algorithm.IFSAAValidation;

/**
 * View an {@link IFSAutomaton} associated with a {@lin IFSAValidationk} as a {@link Predicate}.
 * 
 * @author zuri
 * @param <ELMNT> Type of elements to test
 */
public class FSAA_as_Predicate<ELMNT, A extends IFSAutomaton<ELMNT>> implements Predicate<ELMNT>
{
	A automaton;

	IFSAAValidation<ELMNT, A> validation;

	public FSAA_as_Predicate(A automaton, IFSAAValidation<ELMNT, A> validation)
	{
		this.automaton  = automaton;
		this.validation = validation;
	}

	@Override
	public boolean test(ELMNT element)
	{
		return validation.test(automaton, element);
	}

}
