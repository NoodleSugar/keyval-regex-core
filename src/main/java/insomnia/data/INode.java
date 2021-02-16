package insomnia.data;

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
