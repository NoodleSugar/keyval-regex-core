package insomnia.rule.tree.node;

import java.util.ArrayList;
import java.util.List;

import insomnia.rule.tree.edge.Edge;
import insomnia.rule.tree.edge.IEdge;
import insomnia.rule.tree.value.Value;

public class TreeNode implements ITreeNode<String>
{
	private Edge parent;
	private List<Edge> children;
	private Value value;
	private boolean root;
	private boolean leaf;
	
	public TreeNode(Edge parent, boolean root)
	{
		this.parent = parent;
		this.children = new ArrayList<>();
		this.root = root;
		this.leaf = false;
		this.value = null;
	}
	
	public TreeNode(Edge parent, boolean root, Value value)
	{
		this(parent, root);
		this.leaf = true;
		this.value = value;
	}
	
	public void addChild(Edge child)
	{
		children.add(child);
	}
	
	@Override
	public List<Edge> getParents()
	{
		ArrayList<Edge> a = new ArrayList<>();
		a.add(parent);
		
		return a;
	}

	@Override
	public List<Edge> getChildren()
	{
		return children;
	}

	@Override
	public IEdge<String> getParent()
	{
		return parent;
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
	public Value getValue()
	{
		return value;
	}

}
