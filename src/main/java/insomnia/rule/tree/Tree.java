package insomnia.rule.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import insomnia.rule.tree.TreeBuilder.EdgeData;
import insomnia.rule.tree.edge.Edge;
import insomnia.rule.tree.node.TreeNode;
import insomnia.rule.tree.value.Value;

public class Tree implements ITree<String>
{
	private TreeNode root;
	private ArrayList<TreeNode> leaves;

	protected Tree(TreeBuilder builder)
	{
		leaves = new ArrayList<>();
		HashMap<Integer, Value> leavesD = builder.leaves;

		if(leavesD.containsKey(0))
		{
			root = new TreeNode(null, builder.isRooted, leavesD.get(0));
			leaves.add(root);
		}
		else
			root = new TreeNode(null, builder.isRooted);

		ArrayList<EdgeData> edgesD = builder.edges.get(0);
		for(EdgeData edgeD : edgesD)
		{
			Edge edge = new Edge(edgeD.label);
			TreeNode child = construct(builder, edgeD.child, edge);

			edge.setParent(root);
			edge.setChild(child);
			root.addChild(edge);
		}
	}

	private TreeNode construct(TreeBuilder builder, int n, Edge parent)
	{
		TreeNode node;

		HashMap<Integer, Value> leavesD = builder.leaves;

		if(leavesD.containsKey(n))
		{
			node = new TreeNode(null, builder.isRooted, leavesD.get(n));
			leaves.add(node);
		}
		else
			node = new TreeNode(null, builder.isRooted);

		ArrayList<EdgeData> edgesD = builder.edges.get(n);
		for(EdgeData edgeD : edgesD)
		{
			Edge edge = new Edge(edgeD.label);
			TreeNode child = construct(builder, edgeD.child, edge);

			edge.setParent(node);
			edge.setChild(child);
			node.addChild(edge);
		}
		return node;
	}

	@Override
	public TreeNode getRoot()
	{
		return root;
	}

	@Override
	public List<TreeNode> getLeaves()
	{
		return leaves;
	}

	@Override
	public boolean isRooted()
	{
		return root.isRoot();
	}

}
