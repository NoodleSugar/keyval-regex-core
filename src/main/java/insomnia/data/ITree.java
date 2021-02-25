package insomnia.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * Representation of an immutable Tree.
 * A tree possesses nodes and edges that may be shared between some other trees.
 * Thus, that is the tree that is in charged of obtaining informations about nodes (eg. getChildren()) in its context.
 * <br>
 * <em>A tree must have at least a root node.</em>
 * 
 * @author zuri
 * @param <VAL> Type of the values from nodes.
 * @param <LBL> Type of the labels from edges.
 */
public interface ITree<VAL, LBL>
{
	/**
	 * @return The root of the tree.
	 */
	INode<VAL, LBL> getRoot();

	/**
	 * No edges are present.
	 */
	boolean isEmpty();

	/**
	 * The root must be a true root.
	 */
	boolean isRooted();

	List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node);

	Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node);

	Collection<LBL> getVocabulary();

	// =========================================================================

	public static <VAL, LBL> String toString(ITree<VAL, LBL> tree)
	{
		StringBuilder sb = new StringBuilder();
		toString(sb, 0, tree, tree.getRoot());
		return sb.toString();
	}

	static final String toString_spaces = " ";

	static <VAL, LBL> void toString(StringBuilder sb, int depth, ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		String prefixSpaces = StringUtils.repeat(toString_spaces, depth);

		sb.append("<").append(node).append(">");
		sb.append("\n");

		for (IEdge<VAL, LBL> child : tree.getChildren(node))
		{
			sb.append(prefixSpaces).append(toString_spaces).append(child.getLabel()).append(" ");
			toString(sb, depth + 1, tree, child.getChild());
		}
	}
}