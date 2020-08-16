package insomnia.rule.tree.node;

import java.util.ArrayList;
import java.util.List;

import insomnia.rule.tree.edge.Edge;

public class PathNode implements IPathNode<String>
{
	private Edge parent;
	private Edge child;
	private boolean root;
	private boolean leaf;

	public PathNode()
	{
		this(null, null, false, false);
	}

	public PathNode(PathNode parent, String label)
	{
		this(parent, label, false, false);
	}

	public PathNode(PathNode parent, String label, boolean isRoot, boolean isLeaf)
	{
		this.parent = new Edge(parent, this, label);
		child = null;
		root = isRoot;
		leaf = isLeaf;
	}

	public void setChild(PathNode child)
	{
		this.child = child.parent;
	}

	@Override
	public boolean isRoot()
	{
		return root;
	}

	@Override
	public boolean isLeaf()
	{
		return leaf;
	}

	@Override
	public List<Edge> getParents()
	{
		List<Edge> p = new ArrayList<>();
		p.add(parent);
		return p;
	}

	@Override
	public List<Edge> getChildren()
	{
		List<Edge> c = new ArrayList<>();
		c.add(child);
		return c;
	}

	@Override
	public Edge getParent()
	{
		return parent;
	}

	@Override
	public Edge getChild()
	{
		return child;
	}
}