package insomnia.data.creational;

import java.util.Collection;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

/**
 * A builder that select existing {@link INode}/{@link IEdge} from a parent {@link ITree}.
 * <p>
 * The builder may not takes care of the consistency of the builded sub-tree; that is the represented tree in memory may not be a tree
 * (eg. it may stores disconnected edges, or even a tree above the internal root).
 * Nevertheless, the tree to be considered as the valid represented tree is the sub-tree below the internal root.
 * 
 * @author zuri
 * @param <VAL> type of node value
 * @param <LBL> type of edge label
 */
public interface ISubTreeBuilder<VAL, LBL> extends ITree<VAL, LBL>
{
	/**
	 * Reset the builder on the current internal parent {@link ITree} taking its root as internal root
	 * 
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> reset();

	/**
	 * Reset the builder with the new {@link ITree} {@code parentTree}
	 * 
	 * @param parentTree the new parent tree
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> reset(ITree<VAL, LBL> parentTree);

	/**
	 * Reset the builder with the new {@link ITree} {@code parentTree}, taking a node to be the root
	 * 
	 * @param parentTree the new parent tree
	 * @param root       the new internal root
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> reset(ITree<VAL, LBL> parentTree, INode<VAL, LBL> root);

	/**
	 * Add the sub tree rooted by the current node and stay on the current node.
	 * 
	 * @param root the root of the sub-tree to add.
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> addTree(INode<VAL, LBL> root);

	/**
	 * @param tree the tree to consider; must be a subtree of the initial parent tree
	 * @param root the root of the sub-tree to add
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> addTree(ITree<VAL, LBL> tree, INode<VAL, LBL> root);

	/**
	 * Change the internal root of the builder
	 * 
	 * @param root the new internal root
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> setRoot(INode<VAL, LBL> root);

	/**
	 * Add an edge from the parent tree to the builder
	 * 
	 * @param edge the edge to add
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> add(IEdge<VAL, LBL> edge);

	/**
	 * Add some edges from the parent tree to the builder
	 * 
	 * @param edges the edges to add
	 * @return the builder
	 */
	ISubTreeBuilder<VAL, LBL> add(Collection<IEdge<VAL, LBL>> edges);
}
