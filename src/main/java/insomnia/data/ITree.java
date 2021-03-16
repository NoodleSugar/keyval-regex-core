package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Stack;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.implem.data.Paths;
import insomnia.lib.help.HelpLists;

/**
 * Representation of an immutable Tree.
 * <p>
 * A tree possesses nodes and edges that may be shared between some other trees.
 * Thus, that is the tree that is in charged of obtaining informations about nodes (eg. getChildren()) in its context.
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
	 * Order the state in a way that the lowest states (from leaves) appears before the higher states.
	 */
	public static <VAL, LBL> List<INode<VAL, LBL>> bottomUpOrder(ITree<VAL, LBL> tree)
	{
		return bottomUpOrder(tree, tree.getRoot());
	}

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

	// =========================================================================

	/**
	 * Follow the path given by 'indexes'.
	 * 'indexes' contain the position of the edge to be follow.
	 * An empty 'indexes' refer to the root node.
	 * 
	 * @return the node of 'tree' if exists or {@code null}
	 */
	public static <VAL, LBL> INode<VAL, LBL> followIndex(ITree<VAL, LBL> tree, int... indexes)
	{
		return followIndex(tree, tree.getRoot(), indexes);
	}

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

	// =========================================================================

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
		return Stream.concat(Stream.of(tree.getRoot()), getEdges(tree, root).stream().map(e -> e.getChild())).collect(Collectors.toList());
	}

	/**
	 * Get all the edges of a tree from its root.
	 * <p>
	 * This is a standard implementation that scan the entire tree.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to scan
	 * @param root  the root to consider
	 * @return
	 */
	public static <VAL, LBL> List<IEdge<VAL, LBL>> getEdges(ITree<VAL, LBL> tree)
	{
		return getEdges(tree, tree.getRoot());
	}

	/**
	 * Get all the edges of a tree from one of its nodes.
	 * <p>
	 * This is a standard implementation that scan the entire tree from the given node.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param tree  the tree to scan
	 * @param root  the root to consider
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
			ret.addAll(aedges);
		}
		return ret;
	}

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
		return equals(a, a.getRoot(), b, b.getRoot());
	}

	/**
	 * Check if a tree can be projected on a second one.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param b     second tree
	 * @return true if a project on b
	 * @see IEdge#projectEquals(IEdge, IEdge)
	 * @see INode#projectEquals(INode, INode)
	 */
	public static <VAL, LBL> boolean projectEquals(ITree<VAL, LBL> a, ITree<VAL, LBL> b)
	{
		return projectEquals(a, a.getRoot(), b, b.getRoot());
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
		return structEquals(a, a.getRoot(), b, b.getRoot());
	}

	/**
	 * Check if two trees are equal below two nodes.
	 * <p>
	 * Two trees are equal if they represent the same tree.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param anode node of a to consider to be the root
	 * @param b     second tree
	 * @param bnode node of b to consider to be the root
	 * @return true if a and b are equals
	 */
	public static <VAL, LBL> boolean equals(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode)
	{
		return equals(a, anode, b, bnode, (x, y) -> IEdge.equals(x, y));
	}

	/**
	 * Check if the tree rooted on {@code a} from {@code anode} an be projected on the tree rooted on {@code b} from {@code bnode}
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param anode node of a to consider to be the root
	 * @param b     second tree
	 * @param bnode node of b to consider to be the root
	 * @return true if anode tree project on the bnode tree
	 * @see IEdge#projectEquals(IEdge, IEdge)
	 * @see INode#projectEquals(INode, INode)
	 */
	public static <VAL, LBL> boolean projectEquals(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode)
	{
		return equals(a, anode, b, bnode, IEdge::projectEquals);
	}

	/**
	 * Check if two trees rooted on some nodes have the same structure.
	 * 
	 * @param <VAL> type of node value
	 * @param <LBL> type of edge label
	 * @param a     first tree
	 * @param anode node of a to consider to be the root
	 * @param b     second tree
	 * @param bnode node of b to consider to be the root
	 * @return true if anode and bnode tree have the same structure
	 * @see IEdge#structEquals(IEdge, IEdge)
	 * @see INode#structEquals(INode, INode)
	 */
	public static <VAL, LBL> boolean structEquals(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode)
	{
		return equals(a, anode, b, bnode, IEdge::structEquals);
	}

	/**
	 * Check if two trees are equal below two nodes considering an edge {@link Comparator}.
	 * 
	 * @param <VAL>          type of node value
	 * @param <LBL>          type of edge label
	 * @param a              the tree to search for
	 * @param anode          the node of a to consider as a root
	 * @param b              the tree to search in
	 * @param bnode          the node of b to consider as a root
	 * @param edgeComparator a comparator of {@link IEdge}
	 * @return
	 */
	static <VAL, LBL> boolean equalsMap(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode, //
		BiPredicate<IEdge<VAL, LBL>, IEdge<VAL, LBL>> fequals, //
		Function<IEdge<VAL, LBL>, Integer> fhashCode)
	{
		Map<IEdge<VAL, LBL>, IEdge<VAL, LBL>> bs     = new HashMap<>();
		Queue<IEdge<VAL, LBL>>                aedges = new LinkedList<>();
		aedges.addAll(a.getChildren(anode));

		for (IEdge<VAL, LBL> bchild : b.getChildren(bnode))
			bs.put(IEdgeEquals.create(bchild, fequals, fhashCode), bchild);

		while (!aedges.isEmpty())
		{
			IEdge<VAL, LBL> achild = aedges.poll(), //
				bchild = bs.get(achild);

			if (null == bchild)
				return false;

			bs.remove(achild);
			aedges.addAll(a.getChildren(achild.getChild()));

			for (IEdge<VAL, LBL> bsubchild : b.getChildren(bchild.getChild()))
				bs.put(IEdgeEquals.create(bchild, fequals, fhashCode), bsubchild);
		}
		return true;
	}

	static <VAL, LBL> boolean equals(ITree<VAL, LBL> a, INode<VAL, LBL> anode, ITree<VAL, LBL> b, INode<VAL, LBL> bnode, //
		BiPredicate<IEdge<VAL, LBL>, IEdge<VAL, LBL>> fequals)
	{
		Queue<List<IEdgeEquals<VAL, LBL>>> bedges        = new LinkedList<>();
		Queue<IEdgeEquals<VAL, LBL>>       aedges        = new LinkedList<>();
		List<IEdge<VAL, LBL>>              achilds, bchilds;
		List<IEdgeEquals<VAL, LBL>>        bcurrentEdges = null, tmp;

		achilds = a.getChildren(anode);
		bchilds = b.getChildren(bnode);

		if (achilds.size() != bchilds.size())
			return false;

		for (IEdge<VAL, LBL> achild : achilds)
			aedges.add(IEdgeEquals.create(achild, fequals, (e) -> 0));

		bcurrentEdges = new ArrayList<>(bchilds.size());
		for (IEdge<VAL, LBL> bchild : bchilds)
			bcurrentEdges.add(IEdgeEquals.create(bchild, fequals, (e) -> 0));

		while (!aedges.isEmpty())
		{
			IEdgeEquals<VAL, LBL> achild = aedges.poll() //
				, bchild;

			int bindex = bcurrentEdges.indexOf(achild);

			if (-1 == bindex)
				return false;

			bchild = bcurrentEdges.get(bindex);
			bcurrentEdges.remove(bindex);

			achilds = a.getChildren(achild.getChild());
			bchilds = b.getChildren(bchild.getChild());

			if (achilds.size() != bchilds.size())
				return false;
			if (achilds.size() > 0)
			{
				for (IEdge<VAL, LBL> asubchild : achilds)
					aedges.add(IEdgeEquals.create(asubchild, fequals, (e) -> 0));

				tmp = new ArrayList<>(bchilds.size());
				for (IEdge<VAL, LBL> bsubchild : bchilds)
					tmp.add(IEdgeEquals.create(bsubchild, fequals, (e) -> 0));
				bedges.add(tmp);
			}
			if (bcurrentEdges.isEmpty() && !bedges.isEmpty())
				bcurrentEdges = bedges.poll();
		}
		return true;
	}

	// =========================================================================

	public static <VAL, LBL> String treeOrPathToString(ITree<VAL, LBL> tree)
	{
		if (tree.isPath())
			return IPath.toString(asPath(tree));

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

	static <VAL, LBL> void prettyNode(StringBuilder sb, IEdge<VAL, LBL> edge)
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

	/**
	 * Transform a tree to a Path.
	 * <p>
	 * Note: nothing guarantee that the returned path is a view or not, so the returned path is safe to use if the tree is not modified.
	 * 
	 * @param tree the tree to transform
	 * @return a path equivalent to tree
	 * @throws IllegalArgumentException if tree does not represent a tree
	 */
	// TODO ensure that it's a view ?
	static <VAL, LBL> IPath<VAL, LBL> asPath(ITree<VAL, LBL> tree)
	{
		if (!tree.isPath())
			throw new IllegalArgumentException();

		if (tree instanceof IPath<?, ?>)
			return (IPath<VAL, LBL>) tree;

		ITree<VAL, LBL> safeTree = tree;

		return new AbstractPath<VAL, LBL>()
		{
			ITree<VAL, LBL>       tree;
			List<IEdge<VAL, LBL>> edges;

			// Init
			{
				edges = new ArrayList<>();
				tree  = safeTree;
				INode<VAL, LBL> current = tree.getRoot();

				for (;;)
				{
					List<IEdge<VAL, LBL>> child = tree.getChildren(current);

					if (child.isEmpty())
						break;

					edges.add(child.get(0));
					current = child.get(0).getChild();
				}
				edges = HelpLists.staticList(edges);
			}

			@Override
			public IPath<VAL, LBL> subPath(int from, int to)
			{
				return Paths.create(this, from, to);
			}

			@Override
			public List<IEdge<VAL, LBL>> getEdges()
			{
				return Collections.unmodifiableList(edges);
			}

			@Override
			public List<IEdge<VAL, LBL>> getEdges(INode<VAL, LBL> node)
			{
				return ITree.getEdges(this, node);
			}

			@Override
			public List<LBL> getLabels()
			{
				return CollectionUtils.collect(edges, e -> e.getLabel(), new ArrayList<>());
			}

			@Override
			public List<INode<VAL, LBL>> getNodes()
			{
				List<INode<VAL, LBL>> ret = new ArrayList<>();
				ret.add(tree.getRoot());
				return CollectionUtils.collect(edges, e -> e.getChild(), ret);
			}

			@Override
			public boolean isPath()
			{
				return true;
			}

			@Override
			public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
			{
				return tree.getChildren(node);
			}

			@Override
			public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
			{
				return tree.getParent(node);
			}
		};
	}
}