package insomnia.data;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * An edge in a data.
 * 
 * @author zuri
 * @param <VAL> Type of the value (store in a node)
 * @param <LBL> Type of the label (store in an edge)
 */
public interface IEdge<VAL, LBL>
{
	LBL getLabel();

	INode<VAL, LBL> getParent();

	INode<VAL, LBL> getChild();

	// =========================================================================

	/**
	 * Compare two edges with some predicates.
	 * 
	 * @param a              first edge
	 * @param b              second edge
	 * @param nodePredicate  predicate for edges's nodes
	 * @param labelPredicate predicate for edges's labels
	 * @return 0 if a = b following the predicates, positive if a > 0, or negative if a < b
	 */
	static <VAL, LBL> int compare(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b, BiPredicate<INode<VAL, LBL>, INode<VAL, LBL>> nodePredicate, BiPredicate<LBL, LBL> labelPredicate)
	{
		if (!nodePredicate.test(a.getParent(), b.getParent()))
			return a.getParent().hashCode() - b.getParent().hashCode();
		if (!nodePredicate.test(a.getChild(), b.getChild()))
			return a.getChild().hashCode() - b.getChild().hashCode();
		if (!labelPredicate.test(a.getLabel(), b.getLabel()))
			return a.getLabel().hashCode() - b.getLabel().hashCode();
		return 0;
	}

	/**
	 * @return 0 if edges have the same child/parent objects and equals label, negative value if a < b, or positive value if a > b
	 */
	static <VAL, LBL> int compareSameAs(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return compare(a, b, (x, y) -> x == y, Objects::equals);
	}

	/**
	 * @return true if a is the same edge as b
	 * @see #compareSameAs(IEdge, IEdge)
	 */
	static <VAL, LBL> boolean sameAs(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return 0 == compareSameAs(a, b);
	}

	/**
	 * @return 0 if edges are equals
	 * @see INode#equals(INode, INode)
	 */
	static <VAL, LBL> int compareEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return compare(a, b, (x, y) -> INode.equals(x, y), Object::equals);
	}

	/**
	 * @return true if a and b are equal edges
	 * @see #compareEquals(IEdge, IEdge)
	 * @see INode#equals(INode, INode)
	 */
	static <VAL, LBL> boolean equals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return 0 == compareEquals(a, b);
	}

	/**
	 * @return 0 if a and b have the same tree structure
	 * @see INode#structEquals(INode, INode)
	 */
	static <VAL, LBL> int compareStructEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return compare(a, b, INode::structEquals, Object::equals);
	}

	/**
	 * @return true if a and b have the same tree structure
	 * @see #compareStructEquals(IEdge, IEdge)
	 * @see INode#structEquals(INode, INode)
	 */
	static <VAL, LBL> boolean structEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return 0 == compareStructEquals(a, b);
	}

	/**
	 * @return 0 if a can be projected on b
	 * @see INode#projectEquals(INode, INode)
	 */
	static <VAL, LBL> int compareProjectEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return compare(a, b, INode::projectEquals, (x, y) -> x == null || Objects.equals(x, y));
	}

	/**
	 * @return true if a can be projected to b
	 * @see #compareProjectEquals(IEdge, IEdge)
	 * @see INode#projectEquals(INode, INode)
	 */
	static <VAL, LBL> boolean projectEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return 0 == compareProjectEquals(a, b);
	}

	// =========================================================================

	static String toString(IEdge<?, ?> edge)
	{
		return new StringBuilder() //
			.append(edge.getParent()) //
			.append("--[").append(edge.getLabel()).append("]-->") //
			.append(edge.getChild()) //
			.toString();
	}
}
