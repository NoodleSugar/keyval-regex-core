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

	boolean isDeterministic();

	boolean isSynchronous();

	/**
	 * @param element to test
	 * @return the list of the next valid states
	 */
	List<Integer> nextStates(E element);

	List<Integer> nextEpsilonStates();

	List<Integer> getInitialStates();

	List<Integer> getFinalStates();

	int getCurrentState();

	void goToState(int state);
}
