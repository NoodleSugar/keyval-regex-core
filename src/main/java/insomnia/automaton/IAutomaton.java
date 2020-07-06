package insomnia.automaton;

import java.util.List;

import insomnia.automaton.state.IState;

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
	List<IState<E>> nextStates(E element);

	List<IState<E>> nextEpsilonStates();

	List<IState<E>> getInitialStates();

	List<IState<E>> getFinalStates();

	IState<E> getCurrentState();

	void goToState(IState<E> state);
}
