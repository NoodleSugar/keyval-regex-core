package insomnia.data;

import java.util.Optional;

/**
 * A node in a data.
 * 
 * @author zuri
 * @param <VAL> Type of the value (store in a node)
 * @param <LBL> Type of the label (store in an edge)
 */
public interface INode<VAL, LBL>
{
	Optional<VAL> getValue();

	/**
	 * Is the node a true rooted node ?
	 * This information belongs to the node, and do not change even if the node belongs to many trees.
	 */
	boolean isRooted();

	/**
	 * Is the node a true terminal leaf ?
	 * 
	 * @see INode#isRooted()
	 */
	boolean isTerminal();
}
