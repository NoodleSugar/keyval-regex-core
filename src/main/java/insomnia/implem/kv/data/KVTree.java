package insomnia.implem.kv.data;

import java.util.List;

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
	public List<? extends IEdge<V, E>> getChildren(INode<V, E> node)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEdge<V, E> getParent(INode<V, E> node)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
