package insomnia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public final class TreeOp
{
	private TreeOp()
	{
		throw new AssertionError();
	}

	// =========================================================================

	/**
	 * Order the state in a way that the lowest states (from leaves) appears before the higher states.
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> bottomUpOrder(ITree<VAL, LBL> tree)
	{
		return bottomUpOrder(tree, tree.getRoot());
	}

	public static <VAL, LBL> List<INode<VAL, LBL>> bottomUpOrder(ITree<VAL, LBL> tree, INode<VAL, LBL> treeRoot)
	{
		List<INode<VAL, LBL>>  a, b;
		Stack<INode<VAL, LBL>> nodeStack = new Stack<>();
		a = new ArrayList<>();
		b = new ArrayList<>();

		nodeStack.add(treeRoot);

		while (!nodeStack.isEmpty())
		{
			INode<VAL, LBL>       node  = nodeStack.pop();
			List<IEdge<VAL, LBL>> edges = tree.getChildren(node);

			if (edges.isEmpty())
				a.add(node);
			else
			{
				b.add(node);

				for (IEdge<VAL, LBL> edge : edges)
					nodeStack.push(edge.getChild());
			}
		}
		List<INode<VAL, LBL>> ret = new ArrayList<>(a.size() + b.size());
		ret.addAll(a);
		Collections.reverse(b);
		ret.addAll(b);
		return ret;
	}

	// =========================================================================

	/**
	 * Follow the path given by 'indexes'.
	 * 'indexes' contain the position of the edge to be follow.
	 * An empty 'indexes' refer to the root node.
	 * 
	 * @return the node of 'tree' if exists or {@code null}
	 */
	public static <VAL, LBL> INode<VAL, LBL> followIndex(ITree<VAL, LBL> tree, int... indexes)
	{
		INode<VAL, LBL> ret = tree.getRoot();

		for (int i = 0; i < indexes.length; i++)
		{
			int index = indexes[i];

			List<IEdge<VAL, LBL>> edges = tree.getChildren(ret);

			if (edges.size() <= index)
				return null;

			ret = edges.get(index).getChild();
		}
		return ret;
	}
}
