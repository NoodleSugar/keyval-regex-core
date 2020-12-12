package insomnia.fsa;

import java.util.List;

/**
 * E : type of tested elements
 */
public interface IFSAutomaton<E>
{
	/**
	 * @param elements to test
	 * @return true if it succeed
	 * @throws FSAException
	 */
	boolean test(List<E> elements) throws FSAException;

}
