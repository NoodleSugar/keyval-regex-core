package insomnia.automaton;

import java.util.List;

/**
 * E : type of tested elements
 */
public interface IAutomaton<E>
{
	/**
	 * @param elements to test
	 * @return true if it succeed
	 * @throws AutomatonException
	 */
	boolean run(List<E> elements) throws AutomatonException;
}
