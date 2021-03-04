package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.implem.data.Paths;
import insomnia.lib.help.HelpLists;

/**
 * Representation of an immutable Tree.
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

	Collection<LBL> getVocabulary();

	// =========================================================================

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