package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import insomnia.data.creational.ISubTreeBuilder;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.implem.data.Paths;
import insomnia.implem.data.Trees;
import insomnia.implem.data.creational.SubTreeBuilder;
import insomnia.implem.data.creational.TreeBuilder;
import insomnia.implem.data.regex.TreeMatchResultIterator;
import insomnia.implem.fsa.fta.creational.BUFTABuilder;
import insomnia.implem.fsa.fta.creational.BUFTABuilder.Mode;

/**
 * Representation of an immutable Tree.
 * <p>
 * A tree possesses nodes and edges that may be shared between some other trees.
 * Thus, that is the tree that is in charged of obtaining informations about nodes (eg. {@link #getChildren(INode)}) in its context.
 * <br>
 * <em>A tree must have at least a root node.</em>
 * 
 * @author zuri
 * @param <VAL> Type of the values from nodes.
 * @param <LBL> Type of the labels from edges.
 */
public interface ITree<VAL, LBL>
{
	/**
	 * @return The root of the tree.
	 */
	INode<VAL, LBL> getRoot();

	/**
	 * The tree has a path structure.
	 */
	boolean isPath();

	/**
	 * No edges are present.
	 */
	boolean isEmpty();

	/**
	 * The root must be a true root.
	 */
	boolean isRooted();

	/**
	 * Is the node a leaf?
	 * 
	 * @param node the node to check
	 * @return true if node is a leaf
	 */
	boolean isLeaf(INode<VAL, LBL> node);

	List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node);

	Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node);

	/**
	 * Get all the nodes of the tree
	 * 
	 * @return the nodes of the tree
	 */
	List<INode<VAL, LBL>> getNodes();

	/**
	 * Get all the nodes of a subtree.
	 * 
	 * @param node the root of the subtree
	 * @return the nodes of the subtrees
	 */
	List<INode<VAL, LBL>> getNodes(INode<VAL, LBL> node);

	/**
	 * Get all the leaves of the tree
	 * 
	 * @return the leaves of the tree
	 */
	List<INode<VAL, LBL>> getLeaves();

	/**
	 * Get all the leaves of a subtree.
	 * 
	 * @param node the root of the subtree
	 * @return the nodes of the subtrees
	 */
	List<INode<VAL, LBL>> getLeaves(INode<VAL, LBL> node);

	/**
	 * Get all the edges of a tree.
	 * 
	 * @return the edges of the tree
	 */
	List<IEdge<VAL, LBL>> getEdges();

	/**
	 * Get all the edges of a subtree.
	 * 
	 * @param node the root of the subtree
	 * @return the edges of the subtree
	 */
	List<IEdge<VAL, LBL>> getEdges(INode<VAL, LBL> node);

	/**
	 * @return All the different labels of the tree without duplicates.
	 */
	Collection<LBL> getVocabulary();

	// =========================================================================

	/**
	 * Order the states in a way that the lowest states (from leaves) appears before the higher states.
	 * 
	 * @param tree the tree to scan
	 * @return a list of nodes such as a node at a position is a node ascendant of the previous nodes in the list
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> bottomUpOrder(ITree<VAL, LBL> tree)
	{
		return bottomUpOrder(tree, tree.getRoot());
	}

	/**
	 * Order the states in a way that the lowest states (from leaves) appears before the higher states.
	 * 
	 * @param tree     the reference tree
	 * @param treeRoot root of the sub-tree to scan
	 * @return a list of nodes such as a node at a position is a node ascendant of the previous nodes in the list
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> bottomUpOrder(ITree<VAL, LBL> tree, INode<VAL, LBL> treeRoot)
	{
		List<INode<VAL, LBL>>  a, b;
		Stack<INode<VAL, LBL>> nodeStack = new Stack<>();
		a = new ArrayList<>();
		b = new ArrayList<>();

		nodeStack.add(treeRoot);

		while (!nodeStack.isEmpty())
		{
			INode<VAL, LBL>       node  = nodeStack.pop();
			List<IEdge<VAL, LBL>> edges = tree.getChildren(node);

			if (edges.isEmpty())
				a.add(node);
			else
			{
				b.add(node);

				for (IEdge<VAL, LBL> edge : edges)
					nodeStack.push(edge.getChild());
			}
		}
		List<INode<VAL, LBL>> ret = new ArrayList<>(a.size() + b.size());
		ret.addAll(a);
		Collections.reverse(b);
		ret.addAll(b);
		return ret;
	}

	public static <VAL, LBL> Iterable<INode<VAL, LBL>> bottomUpOrder_skipLeaves(ITree<VAL, LBL> tree)
	{
		ListIterator<INode<VAL, LBL>> bottomUpNodes = ITree.bottomUpOrder(tree).listIterator();

		while (bottomUpNodes.hasNext())
		{
			if (!tree.isLeaf(bottomUpNodes.next()))
			{
				bottomUpNodes.previous();
				break;
			}
		}
		return () -> bottomUpNodes;
	}

	/**
	 * Order the state in a way that the higher states (from root) appears before the lower states.
	 * 
	 * @param tree the tree to scan
	 * @return a list of nodes such as a node at a position has all its ascendants on the previous part of the list
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> topDownOrder(ITree<VAL, LBL> tree)
	{
		return topDownOrder(tree, tree.getRoot());
	}

	/**
	 * Order the state in a way that the higher states (from root) appears before the lower states.
	 * 
	 * @param tree     the reference tree
	 * @param treeRoot root of the sub-tree to scan
	 * @return a list of nodes such as a node at a position has all its ascendants on the previous part of the list
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> topDownOrder(ITree<VAL, LBL> tree, INode<VAL, LBL> treeRoot)
	{
		Queue<INode<VAL, LBL>> nodeQueue = new LinkedList<>();
		var                    ret       = new ArrayList<INode<VAL, LBL>>(tree.getNodes().size());

		nodeQueue.add(treeRoot);

		while (!nodeQueue.isEmpty())
		{
			INode<VAL, LBL>       node  = nodeQueue.poll();
			List<IEdge<VAL, LBL>> edges = tree.getChildren(node);
			ret.add(node);

			for (IEdge<VAL, LBL> edge : edges)
				nodeQueue.add(edge.getChild());
		}
		return ret;
	}

	// =========================================================================

	public static <VAL, LBL> List<IPath<VAL, LBL>> getPaths(ITree<VAL, LBL> tree)
	{
		ISubTreeBuilder<VAL, LBL> tbuilder = new SubTreeBuilder<>(tree);
		List<IPath<VAL, LBL>>     ret      = new ArrayList<>(tree.getLeaves().size());

		// TODO better algorithm
		var root = tree.getRoot();

		for (var leaf : tree.getLeaves())
		{
			var edges = parentEdges(tree, leaf, root);
			tbuilder.reset().add(edges);
			ret.add(Paths.subPath(tbuilder));
		}
		return ret;
	}

	// =========================================================================

	public static <VAL, LBL, TOVAL, TOLBL> ITree<TOVAL, TOLBL> update(ITree<VAL, LBL> tree, Function<VAL, TOVAL> mapVal, Function<LBL, TOLBL> mapLabel)
	{
		return update(tree, tree.getRoot(), mapVal, mapLabel);
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL, TOVAL, TOLBL> ITree<TOVAL, TOLBL> update(ITree<VAL, LBL> tree, INode<VAL, LBL> root, Function<VAL, TOVAL> mapVal, Function<LBL, TOLBL> mapLabel)
	{
		breadthFirstWalk((ITree<TOVAL, TOLBL>) tree, (INode<TOVAL, TOLBL>) root //
			, n -> n.setValue(mapVal.apply((VAL) n.getValue())) //
			, e -> e.setLabel(mapLabel.apply((LBL) e.getLabel()))//
		);
		if (INode.sameAs(root, tree.getRoot()))
			return (ITree<TOVAL, TOLBL>) tree;

		return (ITree<TOVAL, TOLBL>) Trees.subTree(tree, root);
	}

	public static <VAL, LBL> void breadthFirstWalk(ITree<VAL, LBL> tree, INode<VAL, LBL> root, Consumer<INode<VAL, LBL>> visitNode, Consumer<IEdge<VAL, LBL>> visitEdge)
	{
		Queue<INode<VAL, LBL>> nodes = new LinkedList<>();

		visitNode.accept(root);

		if (tree.getChildren(root).isEmpty())
			return;

		nodes.add(root);

		while (!nodes.isEmpty())
		{
			for (var edge : tree.getChildren(nodes.poll()))
			{
				var nodeChild  = edge.getChild();
				var edgeChilds = tree.getChildren(nodeChild);
				visitEdge.accept(edge);
				visitNode.accept(nodeChild);

				if (edgeChilds.isEmpty())
					continue;

				nodes.add(nodeChild);
			}
		}
	}

	// =========================================================================

	/**
	 * Get all the redondant sub-trees of a {@link ITree};
	 * that is sub-trees that can be projected to another sub-tree from their root
	 * 
	 * @param tree the tree to scan
	 * @return the redundant sub-trees of {@code tree}
	 */
	public static <VAL, LBL> Collection<ITree<VAL, LBL>> getRedondantSubTrees(ITree<VAL, LBL> tree)
	{
		return getRedondantSubTrees(tree, tree.getRoot());
	}

	/**
	 * Get all the redondant sub-trees of a sub-tree.
	 * 
	 * @param tree the reference tree
	 * @param root the root of the sub-tree to scan
	 * @return the redundant sub-trees of the sub-tree from {@code root}
	 * @see #getRedondantSubTrees(ITree)
	 */
	public static <VAL, LBL> Collection<ITree<VAL, LBL>> getRedondantSubTrees(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		Collection<ITree<VAL, LBL>>         ret       = new ArrayList<>();
		Queue<INode<VAL, LBL>>              nodeQueue = new LinkedList<>();
		ListValuedMap<LBL, ITree<VAL, LBL>> labelMMap = new ArrayListValuedHashMap<>(tree.getNodes().size());
		nodeQueue.add(root);

		while (!nodeQueue.isEmpty())
		{
			var node = nodeQueue.poll();

			for (IEdge<VAL, LBL> childEdge : tree.getChildren(node))
				labelMMap.put(childEdge.getLabel(), Trees.subTreeView(tree, childEdge.getChild()));

			for (LBL key : labelMMap.keySet())
			{
				var childNodes = labelMMap.get(key);
				if (childNodes.size() <= 1)
					continue;

				var childNodesIt = childNodes.iterator();

				while (childNodesIt.hasNext())
				{
					var child = childNodesIt.next();

					if (IterableUtils.matchesAny(childNodes, c -> c != child && ITree.projectOn(child, c)))
					{
						childNodesIt.remove();
						ret.add(child);
						continue;
					}
					var childNode = child.getRoot();
					nodeQueue.add(childNode);
				}
			}
			labelMMap.clear();
		}
		return ret;
	}

	// =========================================================================

	/**
	 * Follow the path given by {@code indexes}
	 * <p>
	 * {@code indexes} contain the position of the edge to be follow.
	 * An empty {@code indexes} refer to the root node.
	 * 
	 * @param tree    the tree to go through
	 * @param indexes the sequence of child positions to follow from the root of the tree
	 * @return the reached node if the path exists, or {@code null}
	 */
	public static <VAL, LBL> INode<VAL, LBL> followIndex(ITree<VAL, LBL> tree, int... indexes)
	{
		return followIndex(tree, tree.getRoot(), indexes);
	}

	/**
	 * Follow the path given by {@code indexes}
	 * <p>
	 * {@code indexes} contain the position of the edge to be follow.
	 * An empty {@code indexes} refer to the root node.
	 * 
	 * @param tree    the tree to go through
	 * @param node    the node from which scan
	 * @param indexes the sequence of child positions to follow from {@code node}
	 * @return the reached node if the path exists, or {@code null}
	 */
	public static <VAL, LBL> INode<VAL, LBL> followIndex(ITree<VAL, LBL> tree, INode<VAL, LBL> node, int... indexes)
	{
		INode<VAL, LBL> ret = node;

		for (int i = 0; i < indexes.length; i++)
		{
			int index = indexes[i];

			List<IEdge<VAL, LBL>> edges = tree.getChildren(ret);

			if (edges.size() <= index)
				return null;

			ret = edges.get(index).getChild();
		}
		return ret;
	}

	public static <VAL, LBL> VAL followUniquePath(ITree<VAL, LBL> tree, List<LBL> path)
	{
		var node = tree.getRoot();

		for (int i = 0, c = path.size(); i < c; i++)
		{
			LBL label     = path.get(i);
			var childEdge = tree.getEdges(node).stream().filter(e -> Objects.equals(label, e.getLabel())).findFirst();

			if (!childEdge.isPresent())
				return null;

			node = childEdge.get().getChild();
		}
		return node.getValue();
	}

	public static <VAL, LBL> VAL followUniquePath(ITree<VAL, LBL> tree, IPath<VAL, LBL> path)
	{
		return followUniquePath(tree, path.getLabels());
	}

	/**
	 * Get the parent reached going through {@code nb} parent nodes.
	 * 
	 * @param tree the tree to go through
	 * @param node the node from which to scan
	 * @param nb   the number of parent nodes to reached
	 * @return the reached {@link INode}
	 * @throws IndexOutOfBoundsException if there is not {@code nb} parent nodes to go through
	 */
	public static <VAL, LBL> INode<VAL, LBL> parentNode(ITree<VAL, LBL> tree, INode<VAL, LBL> node, int nb)
	{
		INode<VAL, LBL> ret = node;

		for (int i = 0; i < nb; i++)
		{
			var parentEdge = tree.getParent(ret);

			if (parentEdge.isEmpty())
				throw new IndexOutOfBoundsException(String.format("nb=%d size=%d", nb, i));

			ret = parentEdge.get().getParent();
		}
		return ret;
	}

	/**
	 * Get all the edges reached going through {@code nb} parent nodes.
	 * 
	 * @param tree the tree to go through
	 * @param node the node from which to scan
	 * @param nb   the number of parent nodes to reached
	 * @return the list of reached {@link IEdge}s during the scan
	 * @throws IndexOutOfBoundsException if there is not {@code nb} parent nodes to go through
	 */
	public static <VAL, LBL> List<IEdge<VAL, LBL>> parentEdges(ITree<VAL, LBL> tree, INode<VAL, LBL> node, int nb)
	{
		if (nb == 0)
			return Collections.emptyList();

		List<IEdge<VAL, LBL>> ret = new ArrayList<>();

		for (int i = 0; i < nb; i++)
		{
			var parentEdge = tree.getParent(node);

			if (parentEdge.isEmpty())
				throw new IndexOutOfBoundsException(String.format("nb=%d size=%d", nb, i));

			ret.add(parentEdge.get());
			node = parentEdge.get().getParent();
		}
		return ret;
	}

	/**
	 * Get all the edges reached going from {@code node} to {@code parent}.
	 * 
	 * @param tree the tree to go through
	 * @param node the node from which to scan
	 * @param nb   the number of parent nodes to reached
	 * @return the list of reached {@link IEdge}s during the scan
	 * @throws IndexOutOfBoundsException if there is not {@code nb} parent nodes to go through
	 */
	public static <VAL, LBL> List<IEdge<VAL, LBL>> parentEdges(ITree<VAL, LBL> tree, INode<VAL, LBL> node, INode<VAL, LBL> parent)
	{
		if (INode.sameAs(node, parent))
			return Collections.emptyList();

		List<IEdge<VAL, LBL>> ret = new ArrayList<>();

		for (;;)
		{
			var parentEdge = tree.getParent(node);

			if (parentEdge.isEmpty())
				throw new IndexOutOfBoundsException(String.format("node=%s parent=%s in %s", node, parent, ITree.toString(tree)));

			ret.add(parentEdge.get());

			if (INode.sameAs(parentEdge.get().getParent(), parent))
				break;

			node = parentEdge.get().getParent();
		}
		return ret;
	}

	// =========================================================================

	/**
	 * Check if a node is a leaf of a tree.
	 * 
	 * @param tree the tree
	 * @param node the node
	 * @return if {@code node} is a leaf of {@code tree}
	 */
	public static <VAL, LBL> boolean isLeaf(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		return tree.getChildren(node).size() == 0;
	}

	/**
	 * Check if a node is separator.
	 * 
	 * @param tree    the tree
	 * @param subTree a sub-tree
	 * @param node    the node to check
	 * @return true if the node is separator
	 */
	public static <VAL, LBL> boolean isSeparator(ITree<VAL, LBL> tree, ITree<VAL, LBL> subTree, INode<VAL, LBL> node)
	{
		return tree.getChildren(node).size() != subTree.getChildren(node).size();
	}

	/**
	 * Scan the entire tree to get its separator nodes.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to consider
	 * @return
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> getSeparators(ITree<VAL, LBL> tree, ITree<VAL, LBL> subTree)
	{
		return CollectionUtils.select(subTree.getNodes(), n -> isSeparator(tree, subTree, n), new ArrayList<>());
	}

	/**
	 * Scan the entire tree to get its nodes.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to consider
	 * @return
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> getNodes(ITree<VAL, LBL> tree)
	{
		return getNodes(tree, tree.getRoot());
	}

	/**
	 * Scan the entire tree below a node to get its nodes.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to consider
	 * @param root  the node to consider as a root
	 * @return
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> getNodes(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		if (tree.isEmpty())
			return Collections.singletonList(root);

		return IEdge.getNodes(tree.getEdges(root));
	}

	/**
	 * Scan the entire tree to get its leaves.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to consider
	 * @return the list of leaves
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> getLeaves(ITree<VAL, LBL> tree)
	{
		return getLeaves(tree, tree.getRoot());
	}

	/**
	 * Scan the entire tree to get its leaves.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to consider
	 * @param root  the node from which to scan
	 * @return the list of leaves
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> getLeaves(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		return tree.getNodes(root).stream().filter(n -> tree.getChildren(n).isEmpty()).collect(Collectors.toList());
	}

	/**
	 * Get all the edges of a tree from its root
	 * <p>
	 * This is a standard implementation that scan the entire tree.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to scan
	 * @return
	 */
	public static <VAL, LBL> List<IEdge<VAL, LBL>> getEdges(ITree<VAL, LBL> tree)
	{
		return getEdges(tree, tree.getRoot());
	}

	/**
	 * Get all the edges of a sub-tree
	 * <p>
	 * This is a standard implementation that scan the entire tree from the given node.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to scan
	 * @param root  the root of the sub-tree to scan
	 * @return
	 */
	public static <VAL, LBL> List<IEdge<VAL, LBL>> getEdges(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		List<IEdge<VAL, LBL>>  ret    = new ArrayList<>();
		Queue<IEdge<VAL, LBL>> aedges = new LinkedList<>();
		ret.addAll(tree.getChildren(root));
		aedges.addAll(ret);

		while (!aedges.isEmpty())
		{
			IEdge<VAL, LBL>             achild = aedges.poll();
			Collection<IEdge<VAL, LBL>> edges  = tree.getChildren(achild.getChild());
			aedges.addAll(edges);
			ret.addAll(edges);
		}
		return ret;
	}

	/**
	 * Get the vocabulary of a tree
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to scan
	 * @return the unique labels of the tree
	 */
	public static <VAL, LBL> Collection<LBL> getVocabulary(ITree<VAL, LBL> tree)
	{
		return getVocabulary(tree, tree.getRoot());
	}

	/**
	 * Get the vocabulary of a sub-tree
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to scan
	 * @param root  the root of the sub-tree to scan
	 * @return the unique labels of the tree
	 */
	public static <VAL, LBL> Collection<LBL> getVocabulary(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		return CollectionUtils.collect(getEdges(tree, root), IEdge::getLabel, new HashSet<>());
	}

	// =========================================================================

	public static <VAL, LBL> boolean sameAs(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		return sameAs(a, a.getRoot(), b, b.getRoot());
	}

	public static <VAL, LBL> boolean sameAs(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode)
	{
		return isSubTreeOf(a, b) && structEquals(a, b);
	}
	// =========================================================================

	/**
	 * Check if the first tree is a sub tree of the second one.
	 * <p>
	 * A sub tree is a tree where all its nodes/edges are the same as another tree ({@link IEdge#sameAs(IEdge, IEdge)}
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     the tree to search for
	 * @param b     the tree to search in
	 * @return true if {@code a} is a sub tree of {@code b}
	 * @see IEdge#sameAs(IEdge, IEdge)
	 */
	public static <VAL, LBL> boolean isSubTreeOf(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		return isSubTreeOf(a, a.getRoot(), b, b.getRoot());
	}

	/**
	 * Check if the first tree is a sub tree of the second one considering two starting nodes.
	 * <p>
	 * A sub tree is a tree where all its nodes/edges are the same as another tree ({@link IEdge#sameAs(IEdge, IEdge)}
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     the tree to search for
	 * @param anode the node of a to consider as a root
	 * @param b     the tree to search in
	 * @param bnode the node of b to consider as a root
	 * @return true if {@code a } is a sub tree of {@code b}
	 * @see IEdge#sameAs(IEdge, IEdge)
	 */
	public static <VAL, LBL> boolean isSubTreeOf(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode)
	{
		LinkedList<IEdgeEquals<VAL, LBL>> bedges = new LinkedList<>();

		for (IEdge<VAL, LBL> bedge : b.getEdges())
			bedges.add(IEdgeEquals.create(bedge, (x, y) -> x == y, (e) -> 0));

		for (IEdge<VAL, LBL> aedge : a.getEdges())
		{
			if (!bedges.removeFirstOccurrence(IEdgeEquals.create(aedge, IEdge::sameAs, (e) -> 0)))
				return false;
		}
		return true;
	}

	// ==========================================================================

	public static <VAL, LBL> boolean isSemiTwigOf(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		if (a == b)
			return true;
		if (!isSubTreeOf(a, b) || a.getChildren(a.getRoot()).size() > 1)
			return false;

		var anodes = ITree.bottomUpOrder(a);
		{
			int size = anodes.size();
			anodes = anodes.subList(0, --size);

			int pos = IterableUtils.indexOf(anodes, n -> a.getChildren(n).size() > 0);

			if (pos == -1)
				return true;

			anodes = anodes.subList(pos, size);
		}

		for (var anode : anodes)
		{
			if (isSeparator(b, a, anode))
				return false;
		}
		return true;
	}

	public static <VAL, LBL> boolean isSemiTwigOf(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode)
	{
		return isSemiTwigOf(Trees.subTree(a, anode), Trees.subTree(b, bnode));
	}

	// ==========================================================================

	public static <VAL, LBL> int hashCode(ITree<VAL, LBL> tree)
	{
		// TODO
		return 0;
	}

	/**
	 * Check if two trees are equal.
	 * <p>
	 * Two trees are equal if they represent the same tree.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param b     second tree
	 * @return true if a and b are equals
	 * @see IEdge#equals(IEdge, IEdge)
	 * @see INode#equals(INode, INode)
	 */
	public static <VAL, LBL> boolean equals(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		if (a.isRooted() != b.isRooted())
			return false;

		var automaton = new BUFTABuilder<>(new TreeBuilder<>(a).setRooted()).setMode(Mode.EQUALITY).create();
		return automaton.matcher(new TreeBuilder<>(b).setRooted()).matches();
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> boolean equals(ITree<VAL, LBL> a, Object b)
	{
		if (b == a)
			return true;
		if (!(b instanceof ITree<?, ?>))
			return false;
		return ITree.equals(a, (ITree<VAL, LBL>) b);
	}

	/**
	 * Check if a tree can be projected on a second one from their root.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param b     second tree
	 * @return true if a project on b
	 * @see IEdge#projectEquals(IEdge, IEdge)
	 * @see INode#projectOn(INode, INode)
	 */
	public static <VAL, LBL> boolean projectOn(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		if (a.isRooted() && !b.isRooted())
			return false;

		var automaton = new BUFTABuilder<>(new TreeBuilder<>(a).setRooted()).setMode(Mode.PROJECTION).create();
		return automaton.matcher(new TreeBuilder<>(b).setRooted()).matches();
	}

	/**
	 * Check if a tree can be projected on a second one which has the same tree structure.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param b     second tree
	 * @return true if a project on b
	 * @see IEdge#projectEquals(IEdge, IEdge)
	 * @see INode#projectOn(INode, INode)
	 */
	public static <VAL, LBL> boolean structProject(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		if (a.isRooted() && !b.isRooted())
			return false;

		var automaton = new BUFTABuilder<>(new TreeBuilder<>(a).setRooted()).setMode(Mode.STRUCTURE_PROJECTION).create();
		return automaton.matcher(new TreeBuilder<>(b).setRooted()).matches();
	}

	/**
	 * Check if a tree can be projected on a sub-tree of another tree.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param b     second tree
	 * @return true if a project on b
	 * @see IEdge#projectEquals(IEdge, IEdge)
	 * @see INode#projectOn(INode, INode)
	 */
	public static <VAL, LBL> boolean included(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		var automaton = new BUFTABuilder<>(a).setMode(Mode.PROJECTION).create();
		return automaton.matcher(b).matches();
	}

	/**
	 * Check if two trees have the same structure.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param b     second tree
	 * @return true if a and b are structurally equivalent
	 * @see IEdge#structEquals(IEdge, IEdge)
	 * @see INode#structEquals(INode, INode)
	 */
	public static <VAL, LBL> boolean structEquals(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		if (a.isEmpty() != b.isEmpty())
			return false;

		var automaton = new BUFTABuilder<>(a).setMode(Mode.STRUCTURE).create();
		return automaton.matcher(b).matches();
	}

	// =========================================================================

	public static <VAL, LBL> boolean isRootedHomomorphismTo(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		if (a.isEmpty())
			return true;

		return isHomomorphismTo(new TreeBuilder<>(a).setRooted(), new TreeBuilder<>(b).setRooted());
	}

	public static <VAL, LBL> boolean isHomomorphismTo(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		if (a.isEmpty())
			return true;

		var automaton = new BUFTABuilder<>(a).setMode(Mode.PROJECTION).createHomomorphic();
		return automaton.matcher(b).matches();
	}

	// =========================================================================

	public static <VAL, LBL> boolean hasSemiTwig(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		return semiTwigsIterator(a, b).hasNext();
	}

	/**
	 * Find the semi-twigs in b that are sub-trees of a.
	 * 
	 * @param a     the first tree
	 * @param anode the root of the subtree of a
	 * @param b     the second tree
	 * @param bnode the root of the subtree of b
	 * @return the results as an iterator of {@link ITreeMatchResult}.
	 *         The {@link ITreeMatchResult#group()} contains a semi-twig of b,
	 *         and {@link ITreeMatchResult#original()} contains the equivalent tree that is a sub-tree of a.
	 */
	public static <VAL, LBL> Iterator<ITreeMatchResult<VAL, LBL>> semiTwigsIterator(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		var automaton = new BUFTABuilder<>(a).setMode(Mode.SEMI_TWIG).create();
		var it        = new TreeMatchResultIterator<>(automaton.matcher(b));
		return IteratorUtils.filteredIterator(it, e -> !e.group().isEmpty());
	}

	/**
	 * Find the semi-twigs in b that are sub-trees of a.
	 * 
	 * @param a     the first tree
	 * @param anode the root of the subtree of a
	 * @param b     the second tree
	 * @param bnode the root of the subtree of b
	 * @return the results as a {@link Collection} of {@link ITreeMatchResult}.
	 *         The {@link ITreeMatchResult#group()} contains a semi-twig of b,
	 *         and {@link ITreeMatchResult#original()} contains the equivalent tree that is a sub-tree of a.
	 */
	public static <VAL, LBL> Collection<ITreeMatchResult<VAL, LBL>> getSemiTwigs(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		return IteratorUtils.toList(semiTwigsIterator(a, b));
	}

	// =========================================================================

	public static <VAL, LBL> String treeOrPathToString(ITree<VAL, LBL> tree)
	{
		if (tree.isPath())
			return IPath.toString(Paths.subPath(tree));

		return toString(tree);
	}

	public static <VAL, LBL> String toString(ITree<VAL, LBL> tree)
	{
		StringBuilder sb = new StringBuilder();
		toString(sb, new StringBuilder(" "), tree, tree.getRoot());
		return sb.toString();
	}

	static <VAL, LBL> void toString(StringBuilder sb, StringBuilder prefixBuilder, ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		int prefixSize = prefixBuilder.length();

		sb.append("<").append(node).append(">");
		sb.append("\n");

		switch (prefixBuilder.charAt(prefixSize - 1))
		{
		case '└':
			prefixBuilder.setCharAt(prefixSize - 1, ' ');
			break;
		case '├':
			prefixBuilder.setCharAt(prefixSize - 1, '│');
			break;
		}
		Iterator<IEdge<VAL, LBL>> childs = tree.getChildren(node).iterator();
		IEdge<VAL, LBL>           current;

		if (!childs.hasNext())
			return;

		current = childs.next();
		prefixBuilder.append(childs.hasNext() ? "├" : "└");
		sb.append(prefixBuilder.toString());
		prettyNode(sb, current);
		toString(sb, prefixBuilder, tree, current.getChild());
		prefixBuilder.setLength(prefixSize + 1);

		if (!childs.hasNext())
			return;

		current = childs.next();

		while (childs.hasNext())
		{
			prefixBuilder.setCharAt(prefixSize, '├');
			sb.append(prefixBuilder.toString());
			prettyNode(sb, current);
			toString(sb, prefixBuilder, tree, current.getChild());
			prefixBuilder.setLength(prefixSize + 1);
			current = childs.next();
		}
		prefixBuilder.setCharAt(prefixSize, '└');
		sb.append(prefixBuilder.toString());
		prettyNode(sb, current);
		toString(sb, prefixBuilder, tree, current.getChild());
	}

	private static <VAL, LBL> void prettyNode(StringBuilder sb, IEdge<VAL, LBL> edge)
	{
		sb.append(edge.getLabel());
		VAL value = edge.getChild().getValue();

		if (null != value)
			sb.append('=').append(value);

		if (edge.getChild().isTerminal())
			sb.append("$");

		sb.append(" ");
	}

	// =========================================================================

	/**
	 * Scan the entire tree to determine if it is a path.
	 * 
	 * @param tree the tree
	 * @return true if tree has a structure of path
	 */
	public static boolean isPath(ITree<?, ?> tree)
	{
		@SuppressWarnings("unchecked")
		ITree<Object, Object> tsafe = (ITree<Object, Object>) tree;
		return isPath(tsafe, tsafe.getRoot());
	}

	/**
	 * Scan the tree from root to determine if it is a path.
	 * 
	 * @param tree the tree
	 * @param root the node from which to scan
	 * @return true if tree has a structure of path
	 */
	public static <VAL, LBL> boolean isPath(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		for (;;)
		{
			List<IEdge<VAL, LBL>> children = tree.getChildren(root);
			int                   csize    = children.size();

			if (csize == 0)
				return true;
			if (csize > 1)
				return false;
			root = children.get(0).getChild();
		}
	}
}