package insomnia.data.creational;

import insomnia.data.INode;
import insomnia.data.ITree;

/**
 * A builder of a tree.
 * <p>
 * Like {@link IPathBuilder}, this builder has a cursor node, which is used for the same purposes.
 * </p>
 * 
 * @see IPathBuilder
 * @author zuri
 * @param <VAL> type of a node value
 * @param <LBL> type of a label value
 */
public interface ITreeBuilder<VAL, LBL> extends ITree<VAL, LBL>
{
	/**
	 * Reset the builder to its initial state: an empty non rooted tree
	 */
	ITreeBuilder<VAL, LBL> reset();

	/**
	 * Reset the builder to represents the 'src' tree and set the cursor to the root.
	 * 
	 * @param src the tree to represent
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> reset(ITree<VAL, LBL> src);

	/**
	 * Change the rooted nature of the tree
	 * 
	 * @param rooted the rooted value
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> setRooted(boolean rooted);

	// ==========================================================================

	/**
	 * @return the internal cursor node
	 */
	INode<VAL, LBL> getCurrentNode();

	/**
	 * @return the coordinates of the cursor node
	 */
	int[] getCurrentCoordinates();

	/**
	 * Change the cursor to another {@link ITreeBuilder}'s node
	 * 
	 * @param currentNode the new cursor node
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> setCurrentNode(INode<VAL, LBL> currentNode);

	/**
	 * Change the position of the internal cursor node
	 * 
	 * @param pos the new position
	 * @return the builder
	 * @throws IndexOutOfBoundsException if pos is invalid
	 */
	ITreeBuilder<VAL, LBL> setCurrentCoordinates(int... coordinates);

	/**
	 * Change the value of the current node
	 * 
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> setValue(VAL val);

	/**
	 * Set the current node terminal
	 * 
	 * @return the builder
	 * @throws IllegalStateException if the node is not a leaf
	 */
	ITreeBuilder<VAL, LBL> setTerminal(boolean terminal);

	// ==========================================================================

	/**
	 * Add a parent edge.
	 * It does not change the cursor.
	 * 
	 * @param label the label of the edge
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> parent(LBL label);

	/**
	 * Add a parent edge.
	 * It does not change the cursor.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new parent node
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> parent(LBL label, VAL val);

	/**
	 * Add a parent edge and change the cursor to the new parent node.
	 * 
	 * @param label the label of the edge
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> parentUp(LBL label);

	/**
	 * Add a parent edge and change the cursor to the new parent node.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new parent node
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> parentUp(LBL label, VAL val);

	// ==========================================================================

	ITreeBuilder<VAL, LBL> addChild(LBL label);

	/**
	 * Add a new child edge.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new child node
	 * @return the builder
	 * @throws IllegalStateException if the cursor is terminal
	 */
	ITreeBuilder<VAL, LBL> addChild(LBL label, VAL val);

	/**
	 * Add a new child edge.
	 * 
	 * @param label      the label of the edge
	 * @param val        the value of the new child node
	 * @param isTerminal the new node is terminal
	 * @return the builder
	 * @throws IllegalStateException if the cursor is terminal
	 */
	ITreeBuilder<VAL, LBL> addChild(LBL label, VAL val, boolean isTerminal);

	/**
	 * Add a new child edge and set the cursor to the new node.
	 * 
	 * @param label the label of the edge
	 * @return the builder
	 * @throws IllegalStateException if the cursor is terminal
	 */
	ITreeBuilder<VAL, LBL> addChildDown(LBL label);

	/**
	 * Add a new child edge and set the cursor to the new node.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new child node
	 * @return the builder
	 * @throws IllegalStateException if the cursor is terminal
	 */
	ITreeBuilder<VAL, LBL> addChildDown(LBL label, VAL val);

	/**
	 * Add a new child edge and set the cursor to the new node.
	 * 
	 * @param label      the label of the edge
	 * @param val        the value of the new child node
	 * @param isTerminal the new node is terminal
	 * @return the builder
	 * @throws IllegalStateException if the cursor is terminal
	 */
	ITreeBuilder<VAL, LBL> addChildDown(LBL label, VAL val, boolean isTerminal);

	// ==========================================================================

	/**
	 * Add a tree to the cursor.
	 * 
	 * @return the builder
	 * @throws IllegalStateException if the cursor is terminal
	 */
	ITreeBuilder<VAL, LBL> tree(ITree<VAL, LBL> tree);

	/**
	 * Add a tree to the cursor considering {@code treeRoot} as its root.
	 * 
	 * @return the builder
	 * @throws IllegalStateException if the cursor is terminal
	 */
	ITreeBuilder<VAL, LBL> tree(ITree<VAL, LBL> tree, INode<VAL, LBL> treeRoot);

	// ==========================================================================

	/**
	 * Set the cursor to the root.
	 * 
	 * @return the builder
	 */
	ITreeBuilder<VAL, LBL> goToRoot();

	/**
	 * Change the cursor to its parent node.
	 * 
	 * @return the builder
	 * @throws IndexOutOfBoundsException if the cursor is initially on the root
	 */
	ITreeBuilder<VAL, LBL> goUp();

	/**
	 * Change the cursor to its {@code nb}<sup>th</sup> parent node.
	 * 
	 * @param the number of nodes to pass through
	 * @return the builder
	 * @throws IndexOutOfBoundsException if there is no {@code nb} parent nodes
	 */
	ITreeBuilder<VAL, LBL> goUp(int nb);

	/**
	 * Move the cursor to a coordinate from itself.
	 * 
	 * @param coordinates the coordinates to follow
	 * @return the builder
	 * @throws IndexOutOfBoundsException if the coordinates are invalid
	 */
	ITreeBuilder<VAL, LBL> goDown(int... coordinates);
}
