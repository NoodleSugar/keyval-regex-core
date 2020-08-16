package insomnia.rule.tree.node;

import java.util.ArrayList;
import java.util.List;

import insomnia.rule.tree.edge.Edge;
import insomnia.rule.tree.edge.IEdge;

public class TreeNode implements ITreeNode<String>
{
	private Edge parent;
	private List<Edge> children;
	private boolean root;
	private boolean leaf;
	
	public TreeNode(Edge parent, List<Edge> children, boolean root, boolean leaf)
	{
		this.parent = parent;
		this.children = children;
		this.root = root;
		this.leaf = leaf;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLeaf()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
