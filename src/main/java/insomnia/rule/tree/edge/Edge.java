package insomnia.rule.tree.edge;

import insomnia.rule.tree.node.INode;

public class Edge implements IEdge<String>
{
	private String label;
	private INode<String> parent;
	private INode<String> child;

	public Edge(INode<String> parent, INode<String> child, String label)
	{
		this.label = label;
		this.parent = parent;
		this.child = child;
	}
	
	public Edge(String label)
	{
		this(null, null, label);
	}
	
	public void setParent(INode<String> parent)
	{
		this.parent = parent;
	}
	
	public void setChild(INode<String> child)
	{
		this.child = child;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public INode<String> getParent()
	{
		return parent;
	}

	@Override
	public INode<String> getChild()
	{
		return child;
	}
}