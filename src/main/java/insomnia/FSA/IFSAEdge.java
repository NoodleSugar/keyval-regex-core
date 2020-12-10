package insomnia.automaton.edge;

import insomnia.automaton.state.IState;

/**
 * E : type of tested elements
 */
public interface IEdge<E>
{
	boolean isValid(E element);

	IState<E> getParent();
	IState<E> getChild();
}
