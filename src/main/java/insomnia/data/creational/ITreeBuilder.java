package insomnia.data.creational;

import java.util.Collection;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

public interface ITreeBuilder<VAL, LBL> extends ITree<VAL, LBL>
{
	/**
	 * Set the builder to an empty tree.
	 */
	ITreeBuilder<VAL, LBL> reset();

	ITreeBuilder<VAL, LBL> setRooted(boolean rooted);

	ITreeBuilder<VAL, LBL> setCurrentNode(INode<VAL, LBL> currentNode);

	@Override
	boolean isRooted();

	/**
	 * Add an edge before the root without changing the current internal state that may be anywhere.
	 */
	ITreeBuilder<VAL, LBL> parent(LBL label, Collection<? super IEdge<VAL, LBL>> addedEdge);

	ITreeBuilder<VAL, LBL> parent(LBL label);

	ITreeBuilder<VAL, LBL> child(LBL label, Collection<? super IEdge<VAL, LBL>> addedEdge);

	ITreeBuilder<VAL, LBL> child(LBL label);

	/**
	 * Add a tree to the current internal node without changing it.
	 */
	ITreeBuilder<VAL, LBL> tree(ITree<VAL, LBL> tree, INode<VAL, LBL> treeRoot);

	/**
	 * End on an existential leaf.
	 */
	ITreeBuilder<VAL, LBL> end();

	/**
	 * End on a terminal valued leaf.
	 */
	ITreeBuilder<VAL, LBL> endTerminal(VAL value);

	/**
	 * End on a terminal leaf.
	 */
	ITreeBuilder<VAL, LBL> endTerminal();

	// =========================================================================
}
