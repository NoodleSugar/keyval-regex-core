package insomnia.lib.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import insomnia.data.INode;

public interface IGraphEdge<Node>
{
	Node getParent();

	Node getChild();

	// =========================================================================

	public static <Node> List<Node> getNodes(Iterable<? extends IGraphEdge<Node>> edges, Supplier<List<Node>> createList)
	{
		var it = edges.iterator();

		if (!it.hasNext())
			return Collections.emptyList();

		List<Node> ret  = createList.get();
		var        edge = it.next();
		ret.add(edge.getParent());

		for (;;)
		{
			ret.add(edge.getChild());
			if (!it.hasNext())
				break;
			edge = it.next();
		}
		return ret;
	}

	/**
	 * Get {@link INode}s from a {@link List} of edges conserving the order.
	 * 
	 * @param edges the list of edges
	 * @return the nodes from the edges in order
	 */
	public static <Node> List<Node> getNodes(Iterable<? extends IGraphEdge<Node>> edges)
	{
		return getNodes(edges, ArrayList::new);
	}

	/**
	 * Get {@link INode}s from a {@link List} of edges conserving the order.
	 * 
	 * @param edges the list of edges
	 * @return the nodes from the edges in order
	 */
	public static <Node> List<Node> getNodes(Collection<? extends IGraphEdge<Node>> edges)
	{
		return getNodes(edges, () -> new ArrayList<>(edges.size()));
	}
}
