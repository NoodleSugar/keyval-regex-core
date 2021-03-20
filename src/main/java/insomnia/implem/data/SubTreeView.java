package insomnia.implem.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.ListUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.data.ITree;

/**
 * A subTree that is a view on a parent sub-tree.
 * 
 * @author zuri
 */
final class SubTreeView<VAL, LBL> implements ITree<VAL, LBL>
{
	private final ITree<VAL, LBL> parent;
	private final INode<VAL, LBL> baseNode, root;

	SubTreeView(ITree<VAL, LBL> parent, INode<VAL, LBL> baseNode)
	{
		this(parent, baseNode, 0);
	}

	SubTreeView(ITree<VAL, LBL> parent, INode<VAL, LBL> baseNode, int nbParents)
	{
		assert (parent.getNodes().contains(baseNode));
		this.parent   = parent;
		this.baseNode = baseNode;

		if (nbParents == 0)
			root = baseNode;
		else
		{
			var edges = ITree.parentEdges(parent, baseNode, nbParents);
			root = edges.get(0).getParent();
		}
	}

	SubTreeView(ITree<VAL, LBL> parent, INode<VAL, LBL> baseNode, INode<VAL, LBL> root)
	{
		assert (parent.getNodes().contains(baseNode));
		this.parent   = parent;
		this.baseNode = baseNode;
		this.root     = baseNode;
	}

	private IPath<VAL, LBL> parentPath()
	{
		if (root == baseNode)
			return Paths.emptySubPath(this, root);

		return Path.subPath(parent, root, ITree.parentEdges(parent, baseNode, root));
	}

	// =========================================================================

	@Override
	public INode<VAL, LBL> getRoot()
	{
		return root;
	}

	@Override
	public boolean isPath()
	{
		return ITree.isPath(this, baseNode);
	}

	@Override
	public boolean isEmpty()
	{
		return parentPath().isEmpty() && parent.getChildren(baseNode).isEmpty();
	}

	@Override
	public boolean isRooted()
	{
		return getRoot().isRooted();
	}

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		var parentPath = parentPath();

		if (node != baseNode && parentPath.getNodes().contains(node))
			return parentPath.getChildren(node);

		return parent.getChildren(node);
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		var parentPath = parentPath();

		if (parentPath.getNodes().contains(node))
			return parentPath.getParent(node);

		return parent.getParent(node);
	}

	@Override
	public List<INode<VAL, LBL>> getNodes()
	{
		var parentPath = parentPath();
		return ListUtils.union(parentPath.getNodes(), parent.getNodes(baseNode));
	}

	@Override
	public List<INode<VAL, LBL>> getNodes(INode<VAL, LBL> node)
	{
		var parentPath = parentPath();

		if (node != baseNode && parentPath.getNodes().contains(node))
			parentPath.getNodes(node);
		return parent.getNodes(node);
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges()
	{
		var parentPath = parentPath();
		return ListUtils.union(parentPath.getEdges(), parent.getEdges(baseNode));
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges(INode<VAL, LBL> node)
	{
		var parentPath = parentPath();

		if (node != baseNode && parentPath.getNodes().contains(node))
			parentPath.getEdges(node);

		return parent.getEdges(node);
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return ITree.getVocabulary(this);
	}

	// =========================================================================

	@Override
	public String toString()
	{
		return ITree.treeOrPathToString(this);
	}
}
