package insomnia.automaton;

import java.util.List;

import insomnia.automaton.state.IState;

public interface IGAutomaton<E> extends IAutomaton<E>
{
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
