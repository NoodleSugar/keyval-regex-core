package insomnia.FSA;

import java.util.List;
import java.util.function.Predicate;

import insomnia.FSA.algorithm.IFSAAValidation;

/**
 * View an {@link IFSAutomaton} associated with a {@lin IFSAValidationk} as a {@link Predicate}.
 * 
 * @author zuri
 * @param <E> Type of elements to test
 */
public class FSAAPredicate<E, A extends IFSAutomaton<E>> implements Predicate<List<E>>
{
	A automaton;

	IFSAAValidation<E, A> validation;

	public FSAAPredicate(A automaton, IFSAAValidation<E, A> validation)
	{
		this.automaton  = automaton;
		this.validation = validation;
	}

	@Override
	public boolean test(List<E> labels)
	{
		return validation.test(automaton, labels);
	}

}
