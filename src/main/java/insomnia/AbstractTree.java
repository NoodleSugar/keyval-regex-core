package insomnia;

import java.util.Collection;
import java.util.List;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

public abstract class AbstractTree<VAL, LBL> implements ITree<VAL, LBL>
{
	@Override
	public boolean isPath()
	{
		return ITree.isPath(this);
	}

	@Override
	public boolean isEmpty()
	{
		return getChildren(getRoot()).isEmpty() && INode.isEmpty(getRoot());
	}

	@Override
	public boolean isRooted()
	{
		return getRoot().isRooted();
	}

	@Override
	public boolean isLeaf(INode<VAL, LBL> node)
	{
		return ITree.isLeaf(this, node);
	}

	@Override
	public List<INode<VAL, LBL>> getNodes()
	{
		return getNodes(getRoot());
	}

	@Override
	public List<INode<VAL, LBL>> getNodes(INode<VAL, LBL> node)
	{
		return ITree.getNodes(this, node);
	}

	@Override
	public List<INode<VAL, LBL>> getLeaves()
	{
		return getLeaves(getRoot());
	}

	@Override
	public List<INode<VAL, LBL>> getLeaves(INode<VAL, LBL> node)
	{
		return ITree.getLeaves(this, node);
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges()
	{
		return getEdges(getRoot());
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges(INode<VAL, LBL> node)
	{
		return ITree.getEdges(this, node);
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return ITree.getVocabulary(this);
	}

	// =========================================================================

	@Override
	public boolean equals(Object obj)
	{
		return ITree.equals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return ITree.hashCode(this);
	}

	@Override
	public String toString()
	{
		return ITree.treeOrPathToString(this);
	}
}
