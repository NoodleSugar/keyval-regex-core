package insomnia.data;

import java.util.Objects;

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
	 * Check if two nodes are equal.
	 * <p>
	 * Two nodes are equal if they have the same rooted/terminal qualifier, and that their value are equal.
	 * 
	 * @param a the first node
	 * @param b the second node
	 * @return true if a equals b
	 */
	static boolean equals(INode<?, ?> a, INode<?, ?> b)
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
	 * @return true if a and b are structurally equal
	 */
	static boolean structEquals(INode<?, ?> a, INode<?, ?> b)
	{
		return a == b || (Objects.equals(a.getValue(), b.getValue()));
	}

	/**
	 * Check if a node project on a second one.
	 * <p>
	 * A node project on another if it is less constrain for its value and its rooted/terminal nature;
	 * that is its value is null or equals the second one, and rooted/terminal is false, or equals the second one.
	 * 
	 * @param a the first node
	 * @param b the second node
	 * @return true if a project on b
	 */
	static boolean projectEquals(INode<?, ?> a, INode<?, ?> b)
	{
		return a == b ||//
			(a.getValue() == null || Objects.equals(a.getValue(), b.getValue()) //
				&& (!a.isRooted() || b.isRooted()) //
				&& (!a.isTerminal() || b.isTerminal()) //
			);
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
