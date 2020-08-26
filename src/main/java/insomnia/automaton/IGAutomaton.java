package insomnia.automaton;

import java.util.List;

import insomnia.automaton.state.IState;

/*
 * Interface for graphs automatons
 */
public interface IGAutomaton<E> extends IAutomaton<E>
{
	List<IState<E>> getInitialStates();

	List<IState<E>> getFinalStates();

	IState<E> getCurrentState();

	void goToState(IState<E> state);
}
