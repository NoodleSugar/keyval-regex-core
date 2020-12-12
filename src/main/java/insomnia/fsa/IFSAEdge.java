package fsa;

/**
 * E : type of tested elements
 */
public interface IFSAEdge<E>
{
	IFSAState<E> getParent();

	IFSAState<E> getChild();

	IFSALabel<E> getLabel();
}
