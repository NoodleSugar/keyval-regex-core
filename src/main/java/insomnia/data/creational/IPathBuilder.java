package insomnia.data.creational;

import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.implem.data.Paths;

/**
 * A builder of a path.
 * <p>
 * The builder do not create a new {@link IPath} but build himself; it can be view as a mutable {@link IPath}.
 * The purpose is to let other {@link IPath} creational interfaces use its {@link IPath} nature as input; like {@link Paths#create(IPath)}.
 * </p>
 * <p>
 * Many of its methods use an internal {@link INode} that refer to the current node the builder is on; it serves as an internal cursor.
 * Some methods allow to change explicitly this internal node ({@link #setCurrentPos(int)}, {@link #goToLeaf()}, {@link #goToRoot()}, {@link #goUp()}, {@link #goDown()}, etc), and others change it after some actions ({@link #parent(Object)}, {@link #childDown(Object)}, {@link #dropDown()}, {@link #dropDown()}, etc).
 * </p>
 * 
 * @author zuri
 * @param <VAL> type of nodes
 * @param <LBL> type of labels
 */
public interface IPathBuilder<VAL, LBL> extends IPath<VAL, LBL>
{
	/**
	 * Reset the builder to its initial state: an empty non rooted/terminal path
	 */
	IPathBuilder<VAL, LBL> reset();

	/**
	 * Set the builder in the same state as src
	 */
	IPathBuilder<VAL, LBL> reset(IPathBuilder<VAL, LBL> src);

	/**
	 * Change the rooted nature of the path
	 * 
	 * @param rooted the rooted value
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> setRooted(boolean rooted);

	/**
	 * Change the terminal nature of the path
	 * 
	 * @param terminal the terminal value
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> setTerminal(boolean terminal);

	// ==========================================================================

	/**
	 * @return the internal cursor node
	 */
	INode<VAL, LBL> getCurrentNode();

	/**
	 * @return the position of the internal cursor node (the root begin at 0)
	 */
	int getCurrentPos();

	/**
	 * Change the position of the internal cursor node
	 * 
	 * @param pos the new position
	 * @return the builder
	 * @throws IndexOutOfBoundsException if pos is invalid
	 */
	IPathBuilder<VAL, LBL> setCurrentPos(int pos);

	/**
	 * Change the value of the current node
	 * 
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> setValue(VAL val);

	// ==========================================================================

	/**
	 * Add a path above the cursor.
	 * It does not change the cursor.
	 * It does not change the rooted nature of the builder.
	 * 
	 * @param path the path to add
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> parent(IPath<VAL, LBL> path);

	/**
	 * Add a path above the cursor and set the cursor to the added root of '@code path}.
	 * It does not change the rooted nature of the builder.
	 * 
	 * @param path the path to add
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> parentUp(IPath<VAL, LBL> path);

	/**
	 * Add a parent edge.
	 * It does not change the cursor.
	 * 
	 * @param label the label of the edge
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> parent(LBL label);

	/**
	 * Add a parent edge.
	 * It does not change the cursor.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new parent node
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> parent(LBL label, VAL val);

	/**
	 * Add a parent edge and change the cursor to the new parent node.
	 * 
	 * @param label the label of the edge
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> parentUp(LBL label);

	/**
	 * Add a parent edge and change the cursor to the new parent node.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new parent node
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> parentUp(LBL label, VAL val);

	/**
	 * Add a path below the cursor.
	 * It does not change the cursor.
	 * It does not change the terminal nature of the builder.
	 * 
	 * @param path the path to add
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> child(IPath<VAL, LBL> path);

	/**
	 * Add a path below the cursor and set the cursor to the added leaf of '@code path}.
	 * It does not change the terminal nature of the builder.
	 * 
	 * @param path the path to add
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> childDown(IPath<VAL, LBL> path);

	/**
	 * Add a child edge.
	 * It does not change the cursor.
	 * 
	 * @param label the label of the edge
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> child(LBL label);

	/**
	 * Add a child edge.
	 * It does not change the cursor.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new child node
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> child(LBL label, VAL val);

	/**
	 * Add a child edge and change the cursor to the new child node.
	 * 
	 * @param label the label of the edge
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> childDown(LBL label);

	/**
	 * Add a child edge and change the cursor to the new child node.
	 * 
	 * @param label the label of the edge
	 * @param val   the value of the new child node
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> childDown(LBL label, VAL val);

	// ==========================================================================

	/**
	 * Set the cursor to the root.
	 * 
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> goToRoot();

	/**
	 * Set the cursor to the leaf.
	 * 
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> goToLeaf();

	/**
	 * Change the cursor to its parent node.
	 * 
	 * @return the builder
	 * @throws IndexOutOfBoundsException if the cursor is initially on the root
	 */
	IPathBuilder<VAL, LBL> goUp();

	/**
	 * Change the cursor to its {@code nb}<sup>th</sup> parent node.
	 * 
	 * @param the number of nodes to pass through
	 * @return the builder
	 * @throws IndexOutOfBoundsException if there is no {@code nb} parent nodes
	 */
	IPathBuilder<VAL, LBL> goUp(int nb);

	/**
	 * Change the cursor to its child node.
	 * 
	 * @return the builder
	 * @throws IndexOutOfBoundsException if the cursor is initially on the leaf
	 */
	IPathBuilder<VAL, LBL> goDown();

	/**
	 * Change the cursor to its {@code nb}<sup>th</sup> child node.
	 * 
	 * @param the number of nodes to pass through
	 * @return the builder
	 * @throws IndexOutOfBoundsException if there is no {@code nb} child nodes
	 */
	IPathBuilder<VAL, LBL> goDown(int nb);

	// ==========================================================================

	/**
	 * Delete the current node and get up on the parent.
	 * 
	 * @return the builder
	 * @throws IndexOutOfBoundsException if the cursor is initially on the root
	 */
	IPathBuilder<VAL, LBL> dropUp();

	/**
	 * Delete the {@code nbNodes} nodes from the current to the parents.
	 * 
	 * @return the builder
	 * @throws IndexOutOfBoundsException if there is no {@code nbNodes} parent nodes
	 */
	IPathBuilder<VAL, LBL> dropUp(int nbNodes);

	/**
	 * Delete the current node and get down on the child.
	 * 
	 * @return the builder
	 * @throws IndexOutOfBoundsException if the cursor is initially on the leaf
	 */
	IPathBuilder<VAL, LBL> dropDown();

	/**
	 * Delete the nbNodes nodes from the current to the children.
	 * 
	 * @throws IndexOutOfBoundsException if there is no {@code nbNodes} child nodes
	 * @return the builder
	 */
	IPathBuilder<VAL, LBL> dropDown(int nbNodes);
}
