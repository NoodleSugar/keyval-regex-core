package insomnia.rule.tree.node;

import java.util.List;

import insomnia.rule.tree.edge.IEdge;

public interface INode
{
	List<IEdge> getParents();

	List<IEdge> getChildren();

	boolean isRoot();

	boolean isLeaf();
}
