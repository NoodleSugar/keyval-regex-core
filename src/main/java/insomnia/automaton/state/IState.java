package insomnia.automaton.state;

import java.util.Collection;

import insomnia.automaton.edge.IEdge;

/**
 * E : type of tested elements
 */
public interface IState<E> extends Collection<IEdge<E>>
{
	int getId();
	boolean isInitial();
	boolean isFinal();
}
