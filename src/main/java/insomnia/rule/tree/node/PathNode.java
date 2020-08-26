package insomnia.rule.tree.node;

import java.util.ArrayList;
import java.util.List;

import insomnia.rule.tree.edge.Edge;
import insomnia.rule.tree.value.Value;

public class PathNode implements IPathNode<String>
{
	private Edge parent;
	private Edge child;
	private Value value;
	private boolean root;
	private boolean leaf;

	public PathNode(Edge parent, boolean root)
	{
		this.parent = parent;
		this.root = root;
	}

	public PathNode(Edge parent, boolean root, Value value)
	{
		this(parent, root);
		this.leaf = true;
		this.value = value;
	}

	public void setChild(Edge child)
	{
		this.child = child;
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

	@Override
	public Value getValue()
	{
		return value;
	}
}