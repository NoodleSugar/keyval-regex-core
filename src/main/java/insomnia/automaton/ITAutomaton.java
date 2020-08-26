package insomnia.automaton;

import java.util.List;

import insomnia.automaton.state.IValueState;

/*
 * Interface for trees automatons
 */
public interface ITAutomaton<E> extends IAutomaton<E>
{
	List<IValueState<E>> getLeaves();
}
