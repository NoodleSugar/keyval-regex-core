package insomnia.implem.kv.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.jgrapht.graph.SimpleDirectedGraph;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

public class KVTree<V, E> extends SimpleDirectedGraph<V, E> implements ITree<V, E>
{
	private static final long serialVersionUID = 1L;

	private boolean     isRooted;
	private INode<V, E> root;

	public KVTree(Class<? extends E> edgeClass)
	{
		super(edgeClass);
	}

	@Override
	public INode<V, E> getRoot()
	{
		return root;
	}

	@Override
	public boolean isRooted()
	{
		return isRooted;
	}

	@Override
	public List<IEdge<V, E>> getChildren(INode<V, E> node)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IEdge<V, E>> getParent(INode<V, E> node)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<E> getVocabulary()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
