package insomnia.data;

public interface IEdge<V, E>
{
	E getLabel();

	INode<V, E> getParent();

	INode<V, E> getChild();
}
