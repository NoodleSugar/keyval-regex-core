package insomnia.rule.tree.node;

import insomnia.rule.tree.edge.IEdge;

public interface IPathNode extends ITreeNode
{
	IEdge getChild();
}
