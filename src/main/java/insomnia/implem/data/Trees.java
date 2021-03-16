package insomnia.implem.data;

import java.text.ParseException;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.IterableUtils;

import insomnia.data.ITree;
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
			return Paths.create(ITree.asPath(src));

		return new Tree<VAL, LBL>(src);
	}

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

	// =========================================================================

	public static <RVAL, RLBL, VAL, LBL> ITree<RVAL, RLBL> map(ITree<VAL, LBL> src, Function<VAL, RVAL> fmapVal, Function<LBL, RLBL> fmapLabel)
	{
		return Tree.map(src, fmapVal, fmapLabel);
	}
}
