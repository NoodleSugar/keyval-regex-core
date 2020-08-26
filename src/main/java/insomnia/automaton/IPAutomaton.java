package insomnia.automaton;

import java.util.List;

import insomnia.automaton.state.IState;

/*
 * Interface for paths (classic) automatons
 */
public interface IPAutomaton<E> extends IGAutomaton<E>
{
	boolean isDeterministic();

	boolean isSynchronous();
	
	/**
	 * @param element to test
	 * @return the list of the next valid states
	 */
	List<IState<E>> nextStates(E element);

	List<IState<E>> nextEpsilonStates();
}
