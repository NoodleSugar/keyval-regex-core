package insomnia.automaton.state;

import java.util.List;

/*
 * Tree Automaton State that needs to wait a list of states to be validated
 */
public interface IUnionState<E> extends IState<E>
{
	List<IState<E>> getWaitings();
}
