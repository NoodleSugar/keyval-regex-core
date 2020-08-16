package insomnia.rule.tree;

import java.util.List;

import insomnia.rule.tree.edge.Edge;
import insomnia.rule.tree.node.INode;
import insomnia.rule.tree.node.TreeNode;

public class Tree implements ITree<String>
{
	private List<String> labels;
	private List<TreeNode> nodes;
	private TreeNode root;

	@Override
	public TreeNode getRoot()
	{
		return root;
	}

	@Override
	public Edge getParent(INode<String> node)
	{
		return null;
	}

	@Override
	public List<Edge> getChildren(INode<String> node)
	{
		return null;
	}

	@Override
	public boolean isRooted()
	{
		return root.isRoot();
	}

}
