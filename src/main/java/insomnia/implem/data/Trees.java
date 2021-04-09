package insomnia.implem.data;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.creational.ITreeBuilder;
import insomnia.implem.data.creational.SubTreeBuilder;
import insomnia.implem.data.creational.TreeBuilder;
import insomnia.implem.data.regex.parser.IRegexElement;
import insomnia.implem.data.regex.parser.RegexParser;

public final class Trees
{
	private Trees()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private final static Object rootValue = new Object()
	{
		@Override
		public String toString()
		{
			return "^";
		}
	};

	private final static Object terminalValue = new Object()
	{
		@Override
		public String toString()
		{
			return "$";
		}
	};

	@SuppressWarnings("unchecked")
	public static <VAL> VAL getRootValue()
	{
		return (VAL) rootValue;
	}

	@SuppressWarnings("unchecked")
	public static <VAL> VAL getTerminalValue()
	{
		return (VAL) terminalValue;
	}

	// =========================================================================

	private final static RegexParser parser = new RegexParser("''\"\"~~");

	/**
	 * Get the default parser of the package.
	 * <p>
	 * This parser has for delimiters <code>''</code>, <code>""</code> and <code>~~</code>.
	 * 
	 * @return the parser of the package
	 */
	static public <VAL, LBL> RegexParser getParser()
	{
		return parser;
	}

	/**
	 * A path to compare to other path types
	 */
	private final static ITree<?, ?> emptyTree = new Path<>();

	/**
	 * Create an empty {@link Tree}.
	 * The {@link Tree} returned can be compare to other returned empty {@link Tree}s with {@link Tree#equals(Object)}.
	 * 
	 * @see TreeBuilder
	 */
	@SuppressWarnings("unchecked")
	public static <VAL, LBL> ITree<VAL, LBL> empty()
	{
		return (ITree<VAL, LBL>) emptyTree;
	}

	/**
	 * Create a new tree copying informations from 'src'.
	 * 
	 * @see TreeBuilder
	 */
	public static <VAL, LBL> ITree<VAL, LBL> create(ITree<VAL, LBL> src)
	{
		if (src.isPath())
			return Paths.create(Paths.subPath(src));

		return Tree.copy(src, src.getRoot());
	}

	/**
	 * Remove redundancies from a tree.
	 * 
	 * @param tree the tree to consider
	 * @return a tree without redundancies.
	 */
	static public <VAL, LBL> ITree<VAL, LBL> removeRedundancies(ITree<VAL, LBL> tree)
	{
		var tbuilder       = new SubTreeBuilder<>(tree);
		var redundantNodes = CollectionUtils.collect(ITree.getRedondantSubTrees(tree), t -> t.getRoot());

		Queue<INode<VAL, LBL>> nodes = new LinkedList<>();
		nodes.add(tree.getRoot());

		while (!nodes.isEmpty())
		{
			var node = nodes.poll();

			for (var childEdge : tree.getChildren(node))
			{
				var childNode = childEdge.getChild();

				if (redundantNodes.remove(childNode))
					continue;

				tbuilder.add(childEdge);
				nodes.add(childNode);
			}
		}
		return subTree(tbuilder);
	}

	// =========================================================================

	/**
	 * Create a one-node tree.
	 * 
	 * @param tree the parent tree
	 * @param root the node
	 * @return
	 */
	static public <VAL, LBL> ITree<VAL, LBL> emptySubTree(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		return Path.emptySubPath(tree, root);
	}

	/**
	 * Create a snapshot of a tree.
	 * The returned subtree will not be modified if the original subtree changes; that can happens in the case of a {@link ITreeBuilder}.
	 * 
	 * @param tree the parent tree
	 * @return a sub-tree of {@code tree} rooted on {@code node}
	 */
	static public <VAL, LBL> ITree<VAL, LBL> subTree(ITree<VAL, LBL> tree)
	{
		return subTree(tree, tree.getRoot());
	}

	/**
	 * Create a snapshot of a subtree.
	 * The returned subtree will not be modified if the original subtree changes; that can happens in the case of a {@link ITreeBuilder}.
	 * 
	 * @param tree the parent tree
	 * @param node the root node of the sub-tree
	 * @return a sub-tree of {@code tree} rooted on {@code node}
	 */
	static public <VAL, LBL> ITree<VAL, LBL> subTree(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		if (ITree.isPath(tree, node))
			return Paths.subPath(tree, node);

		return Tree.subTree(tree, node);
	}

	/**
	 * Create a view on a tree.
	 * If the subtree changes, the view changes too.
	 * 
	 * @param tree the parent tree
	 * @return a sub-tree view of {@code tree} rooted on {@code node}
	 */
	static public <VAL, LBL> ITree<VAL, LBL> subTreeView(ITree<VAL, LBL> tree)
	{
		return subTreeView(tree, tree.getRoot(), 0);
	}

	/**
	 * Create a view on a sub-tree.
	 * If the subtree changes, the view changes too.
	 * 
	 * @param tree the parent tree
	 * @param node the root node of the sub-tree
	 * @return a sub-tree view of {@code tree} rooted on {@code node}
	 */
	static public <VAL, LBL> ITree<VAL, LBL> subTreeView(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
	{
		return subTreeView(tree, node, 0);
	}

	/**
	 * Create a view on a sub-tree.
	 * If the subtree changes, the view changes too.
	 * 
	 * @param tree      the parent tree
	 * @param node      the root node of the sub-tree
	 * @param nbParents the number of parent nodes to add to the view
	 * @return a sub-tree view of {@code tree} rooted on {@code node}
	 */
	static public <VAL, LBL> ITree<VAL, LBL> subTreeView(ITree<VAL, LBL> tree, INode<VAL, LBL> node, int nbParents)
	{
		return new SubTreeView<>(tree, node, nbParents);
	}

	/**
	 * Create a view on a sub-tree.
	 * If the subtree changes, the view changes too.
	 * 
	 * @param tree the parent tree
	 * @param node the root base of the sub-tree to consider
	 * @param root the root node
	 * @return a sub-tree view of {@code tree} rooted on {@code node}
	 */
	static public <VAL, LBL> ITree<VAL, LBL> subTreeView(ITree<VAL, LBL> tree, INode<VAL, LBL> node, INode<VAL, LBL> root)
	{
		return new SubTreeView<>(tree, node, root);
	}

	// =========================================================================

	/**
	 * Create a tree from an {@link IRegexElement}.
	 * 
	 * @param element  the tree regex element
	 * @param mapValue the map function for values
	 * @param mapLabel the map function for labels
	 * @return The represented tree
	 * @throws IllegalArgumentException if element does not represent a unique tree
	 */
	static public <VAL, LBL> ITree<VAL, LBL> treeFromPRegexElement(IRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		return new TreeFromPRegexElementBuilder<>(mapValue, mapLabel).create(element);
	}

	/**
	 * Get all the trees from an {@link IRegexElement}.
	 * 
	 * @param element  the tree regex element
	 * @param mapValue the map function for values
	 * @param mapLabel the map function for labels
	 * @return The represented trees
	 * @throws IllegalArgumentException if the list is infinite
	 */
	static public <VAL, LBL> List<ITree<VAL, LBL>> treesFromPRegexElement(IRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		return IterableUtils.toList(new TreesFromPRegexElementBuilder<>(element, mapValue, mapLabel));
	}

	/**
	 * Create a tree from a tree regular expression.
	 * 
	 * @see insomnia.implem.data.regex.parser
	 * @param tregex   The tree regular expression
	 * @param mapValue the map function for values
	 * @param mapLabel the map function for labels
	 * @return The represented tree
	 * @throws ParseException           if the regex is invalid
	 * @throws IllegalArgumentException if element does not represent a unique tree
	 */
	public static <VAL, LBL> ITree<VAL, LBL> treeFromString(String tregex, Function<String, VAL> mapValue, Function<String, LBL> mapLabel) throws ParseException
	{
		return treeFromPRegexElement(parser.parse(tregex), mapValue, mapLabel);
	}

	/**
	 * Get all the trees from a tree regular expression.
	 * 
	 * @see insomnia.implem.data.regex.parser
	 * @param tregex   The tree regular expression
	 * @param mapValue the map function for values
	 * @param mapLabel the map function for labels
	 * @return The represented trees
	 * @throws ParseException           if the regex is invalid
	 * @throws IllegalArgumentException if element does not represent a unique tree
	 */
	public static <VAL, LBL> List<ITree<VAL, LBL>> treesFromString(String tregex, Function<String, VAL> mapValue, Function<String, LBL> mapLabel) throws ParseException
	{
		return treesFromPRegexElement(parser.parse(tregex), mapValue, mapLabel);
	}

	/**
	 * Create a tree from a tree regular expression.
	 * 
	 * @see insomnia.implem.data.regex.parser
	 * @param tregex The tree regular expression
	 * @return The represented tree
	 * @throws ParseException           if the regex is invalid
	 * @throws IllegalArgumentException if element does not represent a unique tree
	 */
	public static ITree<String, String> treeFromString(String tregex) throws ParseException
	{
		return treeFromString(tregex, Function.identity(), Function.identity());
	}

	/**
	 * Get all the trees from a tree regular expression.
	 * 
	 * @see insomnia.implem.data.regex.parser
	 * @param tregex The tree regular expression
	 * @return The represented trees
	 * @throws ParseException           if the regex is invalid
	 * @throws IllegalArgumentException if element does not represent a unique tree
	 */
	public static List<ITree<String, String>> treesFromString(String tregex) throws ParseException
	{
		return treesFromString(tregex, Function.identity(), Function.identity());
	}
}
