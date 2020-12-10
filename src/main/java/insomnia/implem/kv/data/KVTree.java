package insomnia.data.tree;

import java.util.List;

import insomnia.data.tree.edge.Edge;
import insomnia.data.tree.node.INode;
import insomnia.data.tree.node.TreeNode;

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
	public boolean isRooted()
	{
		return false;
	}

}
