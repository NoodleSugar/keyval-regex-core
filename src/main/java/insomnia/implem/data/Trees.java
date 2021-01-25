package insomnia.implem.data;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.implem.data.creational.TreeBuilder;

public final class Trees
{
	private Trees()
	{
		throw new AssertionError();
	}

	// =========================================================================

	/**
	 * Create a new tree copying informations from 'src'.
	 * 
	 * @see TreeBuilder
	 */
	public static <VAL, LBL> ITree<VAL, LBL> create(ITree<VAL, LBL> src)
	{
		return new Tree<VAL, LBL>(src);
	}

	// =========================================================================

	public static <RVAL, RLBL, VAL, LBL> ITree<RVAL, RLBL> map(ITree<VAL, LBL> src, Function<VAL, RVAL> fmapVal, Function<LBL, RLBL> fmapLabel)
	{
		return Tree.map(src, fmapVal, fmapLabel);
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
