package insomnia.data;

import org.apache.commons.lang3.ArrayUtils;

import insomnia.lib.help.HelpLists;

/**
 * Utility methods for IPath objects.
 * <p>
 * It contains methods for searching prefix/suffix/inclusion of a path in another.
 * All methods on {@link IPath} takes into account the rooted/terminal nature of the paths.
 * </p>
 * 
 * @author zuri
 */
public final class PathOp
{
	private PathOp()
	{
		throw new AssertionError();
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

		if (PathOp.areSimplyEquals(needle, haystack))
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
