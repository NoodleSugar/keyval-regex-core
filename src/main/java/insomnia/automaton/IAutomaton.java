package insomnia.automaton;

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
	boolean run(E element) throws AutomatonException;
}
