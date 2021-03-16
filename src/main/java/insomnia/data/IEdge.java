package insomnia.data;

import java.util.Objects;

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
	 * Check if a is the same edge as b;
	 * that is the two edges contains the same parent/child {@link Object} node, and labels are equal.
	 * 
	 * @return true if a is the same edge as b
	 */
	static <VAL, LBL> boolean sameAs(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return a.getChild() == b.getChild() //
			&& a.getParent() == b.getParent() //
			&& Objects.equals(a.getLabel(), b.getLabel());
	}

	/**
	 * Check if a and b are equal edges;
	 * that is a.parent/a.child node equals b.parent/b.child ({@link INode#equals(INode, INode)}) , and labels are equal.
	 * 
	 * @return true if a and b are equal edges
	 * @see INode#equals(INode, INode)
	 */
	static <VAL, LBL> boolean equals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return INode.equals(a.getChild(), b.getChild()) //
			&& INode.equals(a.getParent(), b.getParent()) //
			&& Objects.equals(a.getLabel(), b.getLabel());
	}

	/**
	 * Check if a and b have the same tree structure;
	 * that is a.parent/a.child node is structurally equal to b.parent/b.child, and labels are equal.
	 * 
	 * @return true if a and b have the same tree structure
	 * @see INode#structEquals(INode, INode)
	 */
	static <VAL, LBL> boolean structEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return INode.structEquals(a.getChild(), b.getChild()) //
			&& INode.structEquals(a.getParent(), b.getParent()) //
			&& Objects.equals(a.getLabel(), b.getLabel());
	}

	/**
	 * Check if a can be projected on b;
	 * that is a.parent/a.child node can be projected on b.parent/b.child, and a.label == null or a.label is equal to b.label.
	 * 
	 * @return true if a can be projected to b
	 * @see INode#projectEquals(INode, INode)
	 */
	static <VAL, LBL> boolean projectEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return INode.projectEquals(a.getChild(), b.getChild()) //
			&& INode.projectEquals(a.getParent(), b.getParent()) //
			&& (null == a.getLabel() || Objects.equals(a.getLabel(), b.getLabel()));
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
