package insomnia.fsa;

import java.util.function.Predicate;

/**
 * E : type of tested elements
 */
public interface IFSAutomaton<ELMNT> extends Predicate<ELMNT>
{
	/**
	 * @param elements to test
	 * @return true if it succeed
	 * @throws FSAException
	 */
	@Override
	boolean test(ELMNT element);

}
