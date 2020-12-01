package insomnia.data.tree.edge;

import insomnia.data.tree.node.PathNode;

public class Edge implements IEdge<String>
{
	private String label;
	private PathNode parent;
	private PathNode child;

	public Edge(PathNode parent, PathNode child, String label)
	{
		this.label = label;
		this.parent = parent;
		this.child = child;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public PathNode getParent()
	{
		return parent;
	}

	@Override
	public PathNode getChild()
	{
		return child;
	}
}