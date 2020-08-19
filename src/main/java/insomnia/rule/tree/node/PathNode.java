package insomnia.rule.tree.node;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import insomnia.rule.tree.edge.Edge;

public class PathNode implements IPathNode<String>
{
	final private static String emptyLabel = "";

	private Optional<PathNode> parentNode;
	private Optional<Edge>     childEdge;

	public PathNode()
	{
		this(null, emptyLabel);
	}

	public PathNode(PathNode parentNode, String label)
	{
		this.parentNode = Optional.ofNullable(parentNode);
		this.childEdge  = Optional.empty();

		if (parentNode == null)
			return;

		/*
		 * By construction we cannot reuse a node already in a path
		 */
		assert (parentNode.childEdge.isPresent() == false);

		parentNode.childEdge = Optional.of(new Edge(parentNode, this, label));
	}

	@Override
	public List<Edge> getParents()
	{
		Optional<Edge> parentEdge = getParent();

		if (parentEdge.isPresent())
			return Arrays.asList(parentEdge.get());

		return Collections.emptyList();
	}

	@Override
	public List<Edge> getChildren()
	{
		if (childEdge.isPresent())
			return Arrays.asList(childEdge.get());

		return Collections.emptyList();
	}

	@Override
	public Optional<Edge> getParent()
	{
		if (parentNode.isPresent())
			return parentNode.get().getChild();

		return Optional.empty();
	}

	@Override
	public Optional<Edge> getChild()
	{
		return childEdge;
	}
}