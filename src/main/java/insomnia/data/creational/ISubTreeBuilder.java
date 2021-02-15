package insomnia.data.creational;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

public interface ISubTreeBuilder<VAL, LBL> extends ITree<VAL, LBL>
{
	/**
	 * Change the root of the tree to 'root' and set the internal current node to 'root'.
	 */
//	ISubTreeBuilder<VAL, LBL> resetRoot(INode<VAL, LBL> root);

	/**
	 * Reset the builder on the current internal Tree whith its root as internal root.
	 */
	ISubTreeBuilder<VAL, LBL> reset();

	/**
	 * Reset the builder with the new Tree 'parentTree'.
	 */
	ISubTreeBuilder<VAL, LBL> reset(ITree<VAL, LBL> parentTree);

	/**
	 * Add the edge to the tree
	 */
//	ISubTreeBuilder<VAL, LBL> add(IEdge<VAL, LBL> edge);

	/**
	 * Add the sub tree rooted by the current node and stay on the current node.
	 */
	ISubTreeBuilder<VAL, LBL> addTree(INode<VAL, LBL> root);

	ISubTreeBuilder<VAL, LBL> addTree(ITree<VAL, LBL> tree, INode<VAL, LBL> root);

	/**
	 * Change the internal node to the parent node.
	 * If the current internal node is the root the behavior is undefined.
	 */
//	ISubTreeBuilder<VAL, LBL> end();

	// =========================================================================
	// STATELESS OPERATIONS

	ISubTreeBuilder<VAL, LBL> setRoot(INode<VAL, LBL> root);

	ISubTreeBuilder<VAL, LBL> add(IEdge<VAL, LBL> edge);
}
