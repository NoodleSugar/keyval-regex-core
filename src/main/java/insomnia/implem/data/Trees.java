package insomnia.implem.data;


import org.apache.commons.lang3.StringUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

public final class Trees
{
	private Trees()
	{

	}

	// =========================================================================

	public static <VAL, LBL> ITree<VAL, LBL> construct(ITree<VAL, LBL> src)
	{
		return new Tree<VAL, LBL>(src);
	}

	// =========================================================================

	public static <VAL, LBL> String toString(ITree<VAL, LBL> tree)
	{
		StringBuilder sb = new StringBuilder();
		toString(sb, 0, tree, tree.getRoot());
		return sb.toString();
	}

	private static final String toString_spaces = " ";

	private static <VAL, LBL> void toString(StringBuilder sb, int depth, ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		String prefixSpaces = StringUtils.repeat(toString_spaces, depth);

		if (node.isTerminal())
			sb.append("[T]");
		if (node.isRooted())
			sb.append("[R]");

		if (node.getValue().isPresent())
			sb.append("(").append(node.getValue().get()).append(")");

		sb.append("\n");

		for (IEdge<VAL, LBL> child : tree.getChildren(node))
		{
			sb.append(prefixSpaces).append(toString_spaces).append(child.getLabel()).append(" ");
			toString(sb, depth + 1, tree, child.getChild());
		}
	}
}
