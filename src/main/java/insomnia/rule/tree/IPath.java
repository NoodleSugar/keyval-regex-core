package insomnia.rule.tree;

import java.util.List;

import insomnia.rule.tree.edge.IEdge;
import insomnia.rule.tree.node.INode;

public interface IPath<E> extends ITree<E>
{
	List<E> getLabels();

	int getSize();

	boolean isTerminal();

	boolean isIncluded(IPath<E> path);

	boolean isPrefix(IPath<E> path);

	boolean isSuffix(IPath<E> path);

	boolean isEqual(IPath<E> path);

	boolean hasPrefixInSuffix(IPath<E> path);
}