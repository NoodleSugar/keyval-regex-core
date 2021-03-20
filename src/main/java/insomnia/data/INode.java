package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;

/**
 * A node in a data.
 * 
 * @author zuri
 * @param <VAL> Type of the value (store in a node)
 * @param <LBL> Type of the label (store in an edge)
 */
public interface INode<VAL, LBL>
{
	/**
	 * Get the identifier of the node.
	 * If two nodes have the same identifier based on the reference equality {@code ==}, then they are the same.
	 * 
	 * @return the ID of the node
	 */
	Object getID();

	/**
	 * @return the value if set or {@code null}
	 */
	VAL getValue();

	/**
	 * Is the node a true rooted node?
	 * This information belongs to the node, and do not change even if the node belongs to many trees.
	 */
	boolean isRooted();

	/**
	 * Is the node a true terminal leaf?
	 * This information belongs to the node, and do not change even if the node belongs to many trees.
	 */
	boolean isTerminal();

	// =========================================================================

	/**
	 * Check if the node is empty;
	 * that is it is nor rooted nor terminal.
	 * 
	 * @param node the node to check
	 * @return {@code true} if the node is empty, or {@code false}
	 */
	public static boolean isEmpty(INode<?, ?> node)
	{
		return !node.isRooted() && !node.isTerminal();
	}

	// =========================================================================

	/**
	 * Check if two nodes represents the same node;
	 * that is they have the same {@link #getID()} object in memory
	 * 
	 * @param a the first node
	 * @param b the second node
	 * @return {@code true} if a equals b
	 */
	public static boolean sameAs(INode<?, ?> a, INode<?, ?> b)
	{
		return a == b || a.getID() == b.getID();
	}

	/**
	 * Check if two nodes are equal.
	 * <p>
	 * Two nodes are equal if they have the same rooted/terminal qualifier, and that their value are equal.
	 * 
	 * @param a the first node
	 * @param b the second node
	 * @return {@code true} if a equals b
	 */
	public static boolean equals(INode<?, ?> a, INode<?, ?> b)
	{
		return a == b || //
			(Objects.equals(a.getValue(), b.getValue()) //
				&& a.isRooted() == b.isRooted() //
				&& a.isTerminal() == b.isTerminal());
	}

	/**
	 * Check if two nodes are structurally equal.
	 * <p>
	 * Two nodes are structurally equal if their value are equal.
	 * 
	 * @param a the first node
	 * @param b the second node
	 * @return {@code true} if a and b are structurally equal
	 */
	public static boolean structEquals(INode<?, ?> a, INode<?, ?> b)
	{
		return a == b || (Objects.equals(a.getValue(), b.getValue()));
	}

	/**
	 * Check if a node project on a second one.
	 * <p>
	 * A node project on another if it is less constrain for its value and its rooted/terminal nature;
	 * that is its value is null or equals the second one, and rooted/terminal is {@code false}, or equals the second one.
	 * 
	 * @param a the first node
	 * @param b the second node
	 * @return {@code true} if a project on b
	 */
	public static boolean projectEquals(INode<?, ?> a, INode<?, ?> b)
	{
		return a == b ||//
			(a.getValue() == null || Objects.equals(a.getValue(), b.getValue()) //
				&& (!a.isRooted() || b.isRooted()) //
				&& (!a.isTerminal() || b.isTerminal()) //
			);
	}

	// =========================================================================

	/**
	 * Get the values from a sequence of nodes
	 * 
	 * @param nodes the node sequence
	 * @return the values of the nodes in order
	 */
	public static <VAL, LBL> List<VAL> getValues(Iterable<? extends INode<VAL, LBL>> nodes)
	{
		return CollectionUtils.collect(nodes, INode::getValue, new ArrayList<>());
	}

	/**
	 * Get the values from a sequence of nodes
	 * 
	 * @param nodes the node sequence
	 * @return the values of the nodes in order
	 */
	public static <VAL, LBL> List<VAL> getValues(Collection<? extends INode<VAL, LBL>> nodes)
	{
		return CollectionUtils.collect(nodes, INode::getValue, new ArrayList<>(nodes.size()));
	}

	// =========================================================================

	public static String toString(INode<?, ?> node)
	{
		StringBuilder sb = new StringBuilder();

		if (node.isTerminal())
			sb.append("[T]");
		if (node.isRooted())
			sb.append("[R]");

		sb.append(Integer.toHexString(System.identityHashCode(node)));
		Object value = node.getValue();

		if (value != null)
			sb.append("=(").append(value).append(")");

		return sb.toString();
	}
}
