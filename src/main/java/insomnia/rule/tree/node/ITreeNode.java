package insomnia.rule.tree.node;

import insomnia.rule.tree.edge.IEdge;

public interface ITreeNode extends INode
{
	IEdge getParent();
}
