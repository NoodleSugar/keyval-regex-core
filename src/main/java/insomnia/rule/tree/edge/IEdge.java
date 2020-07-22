package insomnia.rule.tree.edge;

import insomnia.rule.tree.node.INode;

public interface IEdge
{
	String getKey();

	INode getParent();

	INode getChild();
}
