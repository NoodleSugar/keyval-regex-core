package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.lib.graph.IGraphEdge;

/**
 * An oriented edge in a data.
 * 
 * @author zuri
 * @param <VAL> Type of the value (store in a node)
 * @param <LBL> Type of the label (store in an edge)
 */
public interface IEdge<VAL, LBL> extends IGraphEdge<INode<VAL, LBL>>
{
	LBL getLabel();

	void setLabel(LBL label);

	@Override
	INode<VAL, LBL> getParent();

	@Override
	INode<VAL, LBL> getChild();

	// =========================================================================

	/**
	 * Check if a is the same edge as b;
	 * that is the two edges contain the same parent/child {@link Object} node, and labels are equal.
	 * 
	 * @return {@code true} if a is the same edge as b
	 * @see INode#sameAs(INode, INode)
	 */
	public static <VAL, LBL> boolean sameAs(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return INode.sameAs(a.getChild(), b.getChild()) //
			&& INode.sameAs(a.getParent(), b.getParent()) //
			&& Objects.equals(a.getLabel(), b.getLabel());
	}

	/**
	 * Check if a and b are equal edges;
	 * that is a.parent/a.child node equals b.parent/b.child ({@link INode#equals(INode, INode)}) , and labels are equal.
	 * 
	 * @return {@code true} if a and b are equal edges
	 * @see INode#equals(INode, INode)
	 */
	public static <VAL, LBL> boolean equals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return INode.equals(a.getChild(), b.getChild()) //
			&& INode.equals(a.getParent(), b.getParent()) //
			&& Objects.equals(a.getLabel(), b.getLabel());
	}

	/**
	 * Define what must be check on a {@link INode#structEquals(INode, INode)} call.
	 * 
	 * @author zuri
	 */
	public enum StructCheck
	{
		/**
		 * Check the nodes's values equality.
		 * 
		 * @see INode#valEquals(INode, INode)
		 */
		VAL

		/**
		 * Check the edges's labels equality.
		 */
		,LABEL
	};

	/**
	 * Check if a and b have the same structure according to {@link StructCheck};
	 * that is a.parent/a.child node is structurally equal to b.parent/b.child and the other checks ask with {@link StructCheck} are {@code true}
	 * 
	 * @return {@code true} if a and b have the same structure
	 * @see INode#structEquals(INode, INode)
	 * @see StructCheck
	 */
	public static <VAL, LBL> boolean structEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b, EnumSet<StructCheck> check)
	{
		var ap = a.getParent();
		var ac = a.getChild();
		var bp = b.getParent();
		var bc = b.getChild();

		return INode.structEquals(ap, bp) && INode.structEquals(ac, bc) //
			&& (!check.contains(StructCheck.LABEL) || Objects.equals(a.getLabel(), b.getLabel())) //
			&& (!check.contains(StructCheck.VAL) || (INode.valEquals(ap, bp) && INode.valEquals(ac, bc)));
	}

	/**
	 * Check if a and b have the same tree structure;
	 * that is a.parent/a.child node is structurally equal to b.parent/b.child, and labels are equal.
	 * 
	 * @return {@code true} if a and b have the same tree structure
	 * @see INode#structEquals(INode, INode)
	 */
	public static <VAL, LBL> boolean structEquals(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return structEquals(a, b, EnumSet.noneOf(StructCheck.class));
	}

	/**
	 * Check if a can be projected on b;
	 * that is a.parent/a.child node can be projected on b.parent/b.child, and a.label == null or a.label is equal to b.label.
	 * 
	 * @return {@code true} if a can be projected to b
	 * @see INode#projectOn(INode, INode)
	 */
	public static <VAL, LBL> boolean projectOn(IEdge<VAL, LBL> a, IEdge<VAL, LBL> b)
	{
		return INode.projectOn(a.getChild(), b.getChild()) //
			&& INode.projectOn(a.getParent(), b.getParent()) //
			&& (null == a.getLabel() || Objects.equals(a.getLabel(), b.getLabel()));
	}

	// =========================================================================

	/**
	 * Get {@link INode}s from a {@link List} of edges conserving the order.
	 * 
	 * @param edges the list of edges
	 * @return the nodes from the edges in order
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> getNodes(Iterable<? extends IEdge<VAL, LBL>> edges)
	{
		return IGraphEdge.getNodes(edges);
	}

	/**
	 * Get {@link INode}s from a {@link List} of edges conserving the order.
	 * 
	 * @param edges the list of edges
	 * @return the nodes from the edges in order
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> getNodes(Collection<? extends IEdge<VAL, LBL>> edges)
	{
		return IGraphEdge.getNodes(edges);
	}

	/**
	 * Get labels from a {@link List} of edges conserving the order.
	 * 
	 * @param edges the list of edges
	 * @return the nodes from the edges in order
	 */
	public static <VAL, LBL> List<LBL> getLabels(Iterable<? extends IEdge<VAL, LBL>> edges)
	{
		return CollectionUtils.collect(edges, IEdge::getLabel, new ArrayList<>());
	}

	/**
	 * Get labels from a {@link List} of edges conserving the order.
	 * 
	 * @param edges the list of edges
	 * @return the nodes from the edges in order
	 */
	public static <VAL, LBL> List<LBL> getLabels(Collection<? extends IEdge<VAL, LBL>> edges)
	{
		return CollectionUtils.collect(edges, IEdge::getLabel, new ArrayList<>(edges.size()));
	}

	// =========================================================================

	public static String toString(IEdge<?, ?> edge)
	{
		return new StringBuilder() //
			.append(edge.getParent()) //
			.append("--[").append(edge.getLabel()).append("]-->") //
			.append(edge.getChild()) //
			.toString();
	}
}
