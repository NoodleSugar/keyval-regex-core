package insomnia.data.tree.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import insomnia.data.tree.edge.Edge;
import insomnia.data.tree.edge.IEdge;

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
	public Optional<? extends IEdge<String>> getParent()
	{
		return Optional.empty();
	}
}
