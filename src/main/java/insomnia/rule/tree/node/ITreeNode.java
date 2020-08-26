package insomnia.rule.tree.node;

import insomnia.rule.tree.edge.IEdge;
import insomnia.rule.tree.value.IValue;

public interface ITreeNode<E> extends INode<E>
{
	IEdge<E> getParent();
	
	boolean isRoot();

	boolean isLeaf();
	
	IValue getValue();
}
