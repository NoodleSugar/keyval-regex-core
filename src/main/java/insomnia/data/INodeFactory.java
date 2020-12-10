package insomnia.data;

public interface INodeFactory<V, E>
{
	INode<V, E> get();

	INode<V, E> get(V value);

	INode<V, E> get(INode<V, E> parent);

	INode<V, E> get(INode<V, E> parent, V value);
}
