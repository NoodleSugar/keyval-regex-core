package insomnia.data;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;

import insomnia.lib.help.HelpLists;

/**
 * An immutable Path.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public interface IPath<VAL, LBL> extends ITree<VAL, LBL>
{
	/**
	 * Such as {@link String#substring(int, int)} get a sub-path from the current one.
	 * The isRooted/isTerminal nature count as a part of the path (like in {@link IPath#size()}.
	 * 
	 * @param begin Inclusive offset.
	 * @param end   Exclusive end index.
	 * @return The sub path.
	 */
	IPath<VAL, LBL> subPath(int from, int to);

	/**
	 * @return the labels of the path conserving the path order
	 */
	List<LBL> getLabels();

	/**
	 * @return nodes in order
	 */
	@Override
	List<INode<VAL, LBL>> getNodes(INode<VAL, LBL> node);

	/**
	 * @return nodes in order
	 */
	@Override
	List<INode<VAL, LBL>> getNodes();

	/**
	 * @return edges in order
	 */
	@Override
	List<IEdge<VAL, LBL>> getEdges(INode<VAL, LBL> node);

	/**
	 * @return edges in order
	 */
	@Override
	List<IEdge<VAL, LBL>> getEdges();

	/**
	 * @return values in order
	 */
	List<VAL> getValues();

	INode<VAL, LBL> getLeaf();

	/**
	 * @return the child edge of 'node' if exists, or {@code null}
	 */
	IEdge<VAL, LBL> getChild(INode<VAL, LBL> node);

	int nbLabels();

	int nbNodes();

	/**
	 * The size of a path is the number of labels plus the isRooted or/and isTerminal natures.
	 * 
	 * @return the size of the path.
	 */
	int size();

	/**
	 * The leaf must be a true leaf.
	 * The value must not be necessary set.
	 */
	boolean isTerminal();

	/**
	 * Rooted and Terminal.
	 */
	boolean isComplete();

	/**
	 * Nor rooted nor terminal.
	 */
	boolean isFree();

	/**
	 * Rooted or Terminal.
	 */
	boolean isFixed();

	// =========================================================================

	/**
	 * Get the informations needed for an {@link IPath#subPath(int, int)} implementation.
	 * 
	 * @param path the path to consider
	 * @param from the inclusive index
	 * @param to   the exclusive index
	 * @return the limits, values and nodes to get to the new path; or null if an empty path must be create
	 */
	public static <VAL, LBL> Triple<RealLimits, List<LBL>, List<INode<VAL, LBL>>> subPathInfos(IPath<VAL, LBL> path, int from, int to)
	{
		assert (from >= 0 && to >= from);

		if (from == to)
			return null;

		RealLimits limits = realLimits(path, from, to);
		return Triple.of(limits, path.getLabels().subList(limits.getFrom(), limits.getTo()), path.getNodes().subList(limits.getFrom(), limits.getTo() + 1));
	}

	// =========================================================================
	static boolean nodeToString(StringBuilder sb, INode<?, ?> node)
	{
		Object value = node.getValue();

		if (value == null)
			return false;

		sb.append("=").append(value);
		return true;
	}

	public static String toString(IPath<?, ?> path)
	{
		StringBuilder sb = new StringBuilder();
		nodeToString(sb, path.getRoot());

		if (0 < sb.length())
		{
			sb.insert(0, "(");
			sb.append(")");
		}
		sb.insert(0, path.getRoot().isRooted() ? "[R]" : "");

		if (0 < sb.length() && path.getLabels().size() > 0)
			sb.append(".");

		if (path.getLabels().size() > 0)
		{
			Iterator<?> nodes = path.getNodes().iterator();
			nodes.next();

			for (Object label : path.getLabels())
			{
				int builderLastLen = sb.length();

				if (null != label)
					sb.append(label);

				if (nodeToString(sb, (INode<?, ?>) nodes.next()))
					sb.insert(builderLastLen, "(").append(")");

				sb.append(".");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		if (path.getLeaf().isTerminal())
			sb.append("[T]");

		return sb.toString();
	}

	// =========================================================================

	public static class Limits
	{
		private final int from, to;

		Limits(int from, int to)
		{
			this.from = from;
			this.to   = to;
		}

		public int getFrom()
		{
			return from;
		}

		public int getTo()
		{
			return to;
		}
	}

	public static class RealLimits extends Limits
	{
		private final boolean isRooted, isTerminal;

		RealLimits(boolean isRooted, boolean isTerminal, int from, int to)
		{
			super(from, to);
			this.isRooted   = isRooted;
			this.isTerminal = isTerminal;
		}

		public boolean isRooted()
		{
			return isRooted;
		}

		public boolean isTerminal()
		{
			return isTerminal;
		}
	}

	/**
	 * Transform the conceptual limits (taking into account isRooted/isTerminal nature) into real limits to refer to a memory location.
	 * 
	 * @param path the path of reference
	 * @param from the starting conceptual point of the path
	 * @param to   the (excluded) ending conceptual point of the path
	 */
	static public RealLimits realLimits(IPath<?, ?> path, int from, int to)
	{
		assert (0 <= from && from < to);
		boolean isRooted   = false;
		boolean isTerminal = false;

		if (path.isRooted())
		{
			if (from > 0)
				from--;
			else
				isRooted = true;
			to--;
		}
		if (path.isTerminal())
		{
			if (to == path.nbLabels() + 1)
			{
				isTerminal = true;
				to--;
			}
		}
		return new RealLimits(isRooted, isTerminal, from, to);
	}

	/**
	 * Return the node of a path located at the index 'index'.
	 * This function does not take into account the isRooted nature of 'path' in the index.
	 * 
	 * @param path  the path to search in
	 * @param index the index where to get the node
	 * @return the index if exists or {@code null}
	 */
	static public <VAL, LBL> INode<VAL, LBL> getRealPathNode(IPath<VAL, LBL> path, int index)
	{
		return path.getNodes().get(index);
	}

	/**
	 * Get the node of a path located at the conceptual location 'from'.
	 * This function take into account the isRooted nature of 'path' in 'from'.
	 * 
	 * @param path the path to search in
	 * @param from the location where to get the node counting the isRooted nature
	 * @return
	 */
	static public <VAL, LBL> INode<VAL, LBL> getPathNode(IPath<VAL, LBL> path, int from)
	{
		RealLimits limits = realLimits(path, from, from + 1);
		return getRealPathNode(path, limits.getFrom());
	}

	// =========================================================================
	// INCLUSION METHODS
	// =========================================================================

	/**
	 * Does it exist an inclusion of needle in haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isAllIncluded(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return isIncluded(needle, haystack, false);
	}

	/**
	 * Does it exist a proper inclusion ({@code !needle.equals(haystack)}) of needle in haystack that is not a simple prefix or suffix of haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isNoPrefSuffIncluded(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return isIncluded(needle, haystack, true);
	}

	/**
	 * Does it exist a proper inclusion ({@code !needle.equals(haystack)}) of needle in haystack?
	 * 
	 * @param needle           The one to search for
	 * @param haystack         The one to search in
	 * @param noSuffixOrPrefix Do not consider a suffix or a prefix of needle in haystack as an inclusion
	 */
	static public boolean isIncluded(IPath<?, ?> needle, IPath<?, ?> haystack, boolean noSuffixOrPrefix)
	{
		return findInclusions(needle, haystack, true, noSuffixOrPrefix).length == 1;
	}

	/**
	 * Find all proper inclusions ({@code !needle.equals(haystack)}) of needle in haystack.
	 * Do not consider a suffix or a prefix of needle in haystack as an inclusion.
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 * @return The positions of each inclusion
	 */
	public static int[] findNoPrefSuffInclusions(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return findInclusions(needle, haystack, true);
	}

	/**
	 * Find all proper inclusions ({@code !needle.equals(haystack)}) of needle in hasystack.
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 * @return The positions of each inclusion
	 */
	public static int[] findAllInclusions(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return findInclusions(needle, haystack, false);
	}

	/**
	 * Find all proper inclusions ({@code !needle.equals(haystack)}) of needle in haystack.
	 * 
	 * @param needle           The one to search for
	 * @param haystack         The one to search in
	 * @param noSuffixOrPrefix Do not consider a suffix or a prefix of needle in haystack as an inclusion
	 * @return The positions of each inclusion
	 */
	public static int[] findInclusions(IPath<?, ?> needle, IPath<?, ?> haystack, boolean noSuffixOrPrefix)
	{
		return findInclusions(needle, haystack, false, noSuffixOrPrefix);
	}

	/**
	 * Find all proper inclusions ({@code !needle.equals(haystack)}) of needle in haystack.
	 * 
	 * @param needle           The one to search for
	 * @param haystack         The one to search in
	 * @param firstFind        Stop the process at the first inclusion founded
	 * @param noSuffixOrPrefix Do not consider a suffix or a prefix of needle in haystack as an inclusion
	 * @return The positions of each inclusion.
	 */
	public static int[] findInclusions(IPath<?, ?> needle, IPath<?, ?> haystack, boolean firstFind, boolean noSuffixOrPrefix)
	{
		if (needle.isRooted())
		{
			if (!noSuffixOrPrefix && isPrefix(needle, haystack, true))
				return new int[] { 0 };

			return ArrayUtils.EMPTY_INT_ARRAY;
		}

		if (needle.isTerminal())
		{
			if (!noSuffixOrPrefix && isSuffix(needle, haystack, true))
				return new int[] { haystack.size() - needle.size() };

			return ArrayUtils.EMPTY_INT_ARRAY;
		}

		if (haystack.isFree())
			return HelpLists.findInclusions(needle.getLabels(), haystack.getLabels(), firstFind, noSuffixOrPrefix);

		if (areSimplyEquals(needle, haystack))
			return new int[] { haystack.isRooted() ? 1 : 0 };

		int[] ret = HelpLists.findInclusions(needle.getLabels(), haystack.getLabels(), firstFind, false);
		int   i;

		if (haystack.isRooted())
		{
			for (i = 0; i < ret.length; i++)
				ret[i]++;

			if (noSuffixOrPrefix && ret.length > 0 && ret[--i] == haystack.size() - needle.size())
				ret = ArrayUtils.remove(ret, i);
		}
		// Haystack.isTerminal
		else if (noSuffixOrPrefix && ret.length > 0 && ret[0] == 0)
			ret = ArrayUtils.remove(ret, 0);

		return ret;
	}
	// =========================================================================
	// PREFIX SUFFIX METHODS
	// =========================================================================

	/**
	 * Is needle a prefix of haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isPrefix(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return isPrefix(needle, haystack, false);
	}

	/**
	 * Is needle a prefix of haystack?
	 * needle must be a proper prefix of haystack, that is {@code !needle.equals(haystack)}
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isProperPrefix(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return isPrefix(needle, haystack, true);
	}

	/**
	 * Is needle a suffix of haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isSuffix(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return isSuffix(needle, haystack, false);
	}

	/**
	 * Is needle a suffix of haystack?
	 * needle must be a proper suffix of haystack, that is {@code !needle.equals(haystack)}
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isProperSuffix(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return isSuffix(needle, haystack, true);
	}

	/**
	 * Is needle a prefix of haystack?
	 * 
	 * @param needle       The one to search for
	 * @param haystack     The one to search in
	 * @param properPrefix needle must be a proper prefix of haystack, that is {@code !needle.equals(haystack)}
	 */
	static public boolean isPrefix(IPath<?, ?> needle, IPath<?, ?> haystack, boolean properPrefix)
	{
		if (haystack.isRooted() != needle.isRooted())
			return false;

		if (needle.isTerminal())
			return !properPrefix && haystack.isTerminal() && areSimplyEquals(needle, haystack);

		// Ask for prefix not proper
		boolean ret = HelpLists.isPrefix(needle.getLabels(), haystack.getLabels(), false);

		// Handle the terminal case if we want proper prefix
		if (ret && properPrefix && needle.getLabels().size() == haystack.getLabels().size())
			return haystack.isTerminal();

		return ret;
	}

	/**
	 * Is needle a suffix of haystack?
	 * 
	 * @param needle       The one to search for
	 * @param haystack     The one to search in
	 * @param properPrefix needle must be a proper suffix of haystack, that is {@code !needle.equals(haystack)}
	 */
	static public boolean isSuffix(IPath<?, ?> needle, IPath<?, ?> haystack, boolean properSuffix)
	{
		if (haystack.isTerminal() != needle.isTerminal())
			return false;

		if (needle.isRooted())
			return !properSuffix && haystack.isRooted() && areSimplyEquals(needle, haystack);

		boolean ret = HelpLists.isSuffix(needle.getLabels(), haystack.getLabels(), false);

		if (ret && properSuffix && needle.getLabels().size() == haystack.getLabels().size())
			return haystack.isRooted();

		return ret;
	}

	/**
	 * Are a and b the same path?
	 */
	static public boolean areEquals(IPath<?, ?> a, IPath<?, ?> b)
	{
		if (a.isRooted() != b.isRooted() || a.isTerminal() != b.isTerminal())
			return false;

		return areSimplyEquals(a, b);
	}

	/**
	 * Check the equality of the path's label sequences.
	 */
	static private boolean areSimplyEquals(IPath<?, ?> a, IPath<?, ?> b)
	{
		return a.getLabels().equals(b.getLabels());
	}

	// =========================================================================
	// FIND ALL POSSIBLE PREFIXES/SUFFIXES
	// =========================================================================

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * needle can't be a prefix of haystack, and haystack can't be a suffix of needle.
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 * @return sizes of prefixes
	 */
	static public int[] findOverlappedPossiblePrefixes(IPath<?, ?> needle, IPath<?, ?> haystack)
	{
		return findOverlappedPossiblePrefixes(needle, haystack, false);
	}

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * needle can't be a prefix of haystack, and haystack can't be a suffix of needle.
	 * 
	 * @param needle    The one to search for
	 * @param haystack  The one to search in
	 * @param firstFind Stop the process at the first one founded
	 * @return sizes of prefixes
	 */
	static public int[] findOverlappedPossiblePrefixes(IPath<?, ?> needle, IPath<?, ?> haystack, boolean firstFind)
	{
		int ret[] = findPossiblePrefixes(needle, haystack, firstFind, true);

		if (ret.length == 0)
			return ArrayUtils.EMPTY_INT_ARRAY;

		int i = ret.length - 1;

		if (ret[i] == needle.size() || ret[i] == haystack.size())
			ret = ArrayUtils.remove(ret, i);

		return ret;
	}

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * 
	 * @param needle       The one to search for
	 * @param haystack     The one to search in
	 * @param properSuffix needle can't be a prefix of haystack
	 * @return sizes of prefixes
	 */
	static public int[] findPossiblePrefixes(IPath<?, ?> needle, IPath<?, ?> haystack, boolean properSuffix)
	{
		return findPossiblePrefixes(needle, haystack, false, properSuffix);
	}

	/**
	 * Fin all prefixes of needle that are suffixes of haystack.
	 * 
	 * @param needle       The one to search for
	 * @param haystack     The one to search in
	 * @param properSuffix needle can't be a suffix of haystack
	 * @return sizes of prefixes
	 */
	static public int[] findPossibleSuffixes(IPath<?, ?> needle, IPath<?, ?> haystack, boolean properSuffix)
	{
		return findPossibleSuffixes(needle, haystack, false, properSuffix);
	}

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * 
	 * @param needle       The one to search for
	 * @param haystack     The one to search in
	 * @param firstFind    Stop the process at the first one founded
	 * @param properPrefix needle can't be a prefix of haystack
	 * @return sizes of prefixes
	 */
	static public int[] findPossiblePrefixes(IPath<?, ?> needle, IPath<?, ?> haystack, boolean firstFind, boolean properPrefix)
	{
		final int nl_size = needle.getLabels().size();
		final int hl_size = haystack.getLabels().size();

		/*
		 * The overlap case
		 */
		if (!needle.isTerminal() && !haystack.isRooted())
		{
			/*
			 * We had compute the non proper suffix prefix.
			 * So we must check if the last element is not the trivial Prefix if we don't want it.
			 */
			int ret[] = HelpLists.findSuffixPrefix(needle.getLabels(), haystack.getLabels(), firstFind, false);

			if (ret.length > 0 && properPrefix //
				&& nl_size == hl_size //
				&& ret[ret.length - 1] == hl_size //
				&& !needle.isRooted() //
				&& !haystack.isTerminal() //
			)
				return ArrayUtils.remove(ret, ret.length - 1);

			return ret;
		}
		/*
		 * Equals or prefix case
		 */
		else if (needle.isRooted() && haystack.isRooted())
		{
			if ((!properPrefix && areEquals(needle, haystack)) || isPrefix(needle, haystack, properPrefix))
				return new int[] { needle.size() };
		}
		/*
		 * Haystack may be suffix case
		 */
		else if (needle.isTerminal() && haystack.isTerminal())
		{
			if (isSuffix(haystack, needle, properPrefix))
				return new int[] { haystack.size() };
		}
		return ArrayUtils.EMPTY_INT_ARRAY;
	}

	/**
	 * Fin all prefixes of needle that are suffixes of haystack.
	 * 
	 * @param needle       The one to search for
	 * @param haystack     The one to search in
	 * @param firstFind    Stop the process at the first one founded
	 * @param properPrefix needle can't be a prefix of haystack
	 * @return sizes of prefixes
	 */
	static public int[] findPossibleSuffixes(IPath<?, ?> needle, IPath<?, ?> haystack, boolean firstFind, boolean properSuffix)
	{
		return findPossiblePrefixes(haystack, needle, firstFind, properSuffix);
	}

}