package insomnia.data.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility methods for IPath objects.
 * It contains methods for searching prefix/suffix/inclusion of a path in another.
 * Methods containing "Simple" in their name do not take care of the rooted/terminal nature of their IPath arguments, and act only on their label sequence.
 * 
 * @author zuri
 */
public final class Paths
{

	public static int nbLabelsFromPrefixSize(IPath<?> p, int size)
	{
		assert (0 < size && size <= p.size());

		if (size == 0)
			return 0;

		if (size == p.size())
			return p.getLabels().size();

		if (p.isRooted())
			return size - 1;

		return size;
	}

	public static int nbLabelsFromSuffixSize(IPath<?> p, int size)
	{
		assert (0 < size && size <= p.size());

		if (size == 0)
			return 0;

		if (size == p.size())
			return p.getLabels().size();

		if (p.isTerminal())
			return size - 1;

		return size;
	}

	public static int labelPosFromPathPos(IPath<?> p, int pos)
	{
		if (p.isRooted())
			return pos - 1;

		return pos;
	}

	// =========================================================================
	// INCLUSION METHODS
	// =========================================================================

	static public <E> boolean isAllIncluded(IPath<E> needle, IPath<E> haystack)
	{
		return isIncluded(needle, haystack, false);
	}

	static public <E> boolean isNoPrefSuffIncluded(IPath<E> needle, IPath<E> haystack)
	{
		return isIncluded(needle, haystack, true);
	}

	static public <E> boolean isIncluded(IPath<E> needle, IPath<E> haystack, boolean noSuffixOrPrefix)
	{
		return findInclusions(needle, haystack, true, noSuffixOrPrefix).length == 1;
	}

	public static <E> int[] findNoPrefSuffInclusions(IPath<E> needle, IPath<E> haystack)
	{
		return findInclusions(needle, haystack, true);
	}

	public static <E> int[] findAllInclusions(IPath<E> needle, IPath<E> haystack)
	{
		return findInclusions(needle, haystack, false);
	}

	public static <E> int[] findInclusions(IPath<E> needle, IPath<E> haystack, boolean noSuffixOrPrefix)
	{
		return findInclusions(needle, haystack, false, noSuffixOrPrefix);
	}

	/**
	 * Search all the proper inclusions (needle != haystack) of needle in haystack taking into account the rooted/terminal nature of needle and haystack.
	 * 
	 * @param needle
	 * @param haystack
	 * @param firstFind        Stop the process at the first inclusion founded
	 * @param noSuffixOrPrefix Do not consider a suffix or a prefix of needle in haystack as an inclusion
	 * @return The positions of each inclusion.
	 */
	public static <E> int[] findInclusions(IPath<E> needle, IPath<E> haystack, boolean firstFind, boolean noSuffixOrPrefix)
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
			return findSimpleInclusions(needle, haystack, firstFind, noSuffixOrPrefix);

		if (Paths.areSimplyEquals(needle, haystack))
			return new int[] { haystack.isRooted() ? 1 : 0 };

		int[] ret = findSimpleInclusions(needle, haystack, firstFind, false);
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

	/**
	 * Search all inclusions of needle in haystack but not the equal inclusion.
	 */
	public static <E> int[] findNoPrefSuffSimpleInclusions(IPath<E> needle, IPath<E> haystack)
	{
		return findSimpleInclusions(needle, haystack, true);
	}

	public static <E> int[] findAllSimpleInclusions(IPath<E> needle, IPath<E> haystack)
	{
		return findSimpleInclusions(needle, haystack, false);
	}

	public static <E> int[] findSimpleInclusions(IPath<E> needle, IPath<E> haystack, boolean noSuffixOrPrefix)
	{
		return findSimpleInclusions(needle, haystack, false, noSuffixOrPrefix);
	}

	/**
	 * Search all the proper inclusions (needle != haystack) of needle in haystack without considering if needle or haystack are 'rooted' or 'terminal'.
	 * 
	 * @param needle
	 * @param haystack
	 * @param firstFind        Stop the process at the first inclusion founded
	 * @param noSuffixOrPrefix Do not consider a proper suffix or prefix of needle in haystack as an inclusion
	 * @return The positions of each inclusion.
	 */
	private static <E> int[] findSimpleInclusions(IPath<E> needle, IPath<E> haystack, boolean firstFind, boolean noSuffixOrPrefix)
	{
		List<E>   n_labels = needle.getLabels();
		List<E>   h_labels = haystack.getLabels();
		final int n_size   = n_labels.size();
		final int h_size   = h_labels.size();

		ArrayList<Integer> ret = new ArrayList<>();

		if (n_size >= h_size)
			return ArrayUtils.EMPTY_INT_ARRAY;

		int h_i;

		/*
		 * No need to check if offset > h_offset_max because n_size become lower than the rest.
		 */
		int h_i_max = h_size - n_size;

		if (noSuffixOrPrefix)
			h_i = 1;
		else
		{
			h_i = 0;
			h_i_max++;
		}

		for (; h_i < h_i_max; h_i++)
		{
			List<E> h_part = h_labels.subList(h_i, h_i + n_size);

			if (h_part.equals(n_labels))
			{
				ret.add(h_i);

				if (firstFind)
					break;
			}
		}
		return ret.stream().mapToInt(Integer::intValue).toArray();
	}

	// =========================================================================
	// PREFIX SUFFIX METHODS
	// =========================================================================

	static public <E> boolean isAllPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return isPrefix(needle, haystack, false);
	}

	static public <E> boolean isProperPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return isPrefix(needle, haystack, true);
	}

	static public <E> boolean isAllSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return isSuffix(needle, haystack, false);
	}

	static public <E> boolean isProperSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return isSuffix(needle, haystack, true);
	}

	static public <E> boolean isPrefix(IPath<E> needle, IPath<E> haystack, boolean properPrefix)
	{
		if (haystack.isRooted() != needle.isRooted())
			return false;

		if (needle.isTerminal())
			return !properPrefix && haystack.isTerminal() && areSimplyEquals(needle, haystack);

		// Ask for prefix not proper
		boolean ret = isSimplePrefix(needle, haystack, false);

		// Handle the terminal case if we want proper prefix
		if (ret && properPrefix && needle.getLabels().size() == haystack.getLabels().size())
			return haystack.isTerminal();

		return ret;
	}

	static public <E> boolean isSuffix(IPath<E> needle, IPath<E> haystack, boolean properSuffix)
	{
		if (haystack.isTerminal() != needle.isTerminal())
			return false;

		if (needle.isRooted())
			return !properSuffix && haystack.isRooted() && areSimplyEquals(needle, haystack);

		boolean ret = isSimpleSuffix(needle, haystack, false);

		if (ret && properSuffix && needle.getLabels().size() == haystack.getLabels().size())
			return haystack.isRooted();

		return ret;
	}

	static public <E> boolean isAllSimplePrefix(IPath<E> needle, IPath<E> haystack)
	{
		return isSimplePrefix(needle, haystack, false);
	}

	static public <E> boolean isProperSimplePrefix(IPath<E> needle, IPath<E> haystack)
	{
		return isSimplePrefix(needle, haystack, true);
	}

	static public <E> boolean isAllSimpleSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return isSimpleSuffix(needle, haystack, false);
	}

	static public <E> boolean isProperSimpleSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return isSimpleSuffix(needle, haystack, true);
	}

	/**
	 * Check if needle is prefix of haystack.
	 * The method do not take care of the isRooted aspects.
	 */
	static public boolean isSimplePrefix(IPath<?> needle, IPath<?> haystack, boolean properPrefix)
	{
		final int n_size = needle.getLabels().size();
		final int h_size = haystack.getLabels().size();

		if (properPrefix && n_size >= h_size //
			|| !properPrefix && n_size > h_size //
		)
			return false;

		return needle.getLabels().equals(haystack.getLabels().subList(0, n_size));
	}

	/**
	 * Check if needle is suffix of haystack.
	 * The method do not take care of the isTerminal aspects.
	 */
	static public boolean isSimpleSuffix(IPath<?> needle, IPath<?> haystack, boolean properSuffix)
	{
		final int n_size = needle.getLabels().size();
		final int h_size = haystack.getLabels().size();

		if (properSuffix && n_size >= h_size //
			|| !properSuffix && n_size > h_size //
		)
			return false;

		return needle.getLabels().equals(haystack.getLabels().subList(h_size - n_size, h_size));
	}

	static public boolean areEquals(IPath<?> a, IPath<?> b)
	{
		if (a.isRooted() != b.isRooted() || a.isTerminal() != b.isTerminal())
			return false;

		return areSimplyEquals(a, b);
	}

	static public boolean areSimplyEquals(IPath<?> a, IPath<?> b)
	{
		return a.getLabels().equals(b.getLabels());
	}

	// =========================================================================
	// FIND ALL POSSIBLE PREFIXES/SUFFIXES
	// =========================================================================

	static public <E> int[] findOverlappedPossiblePrefixes(IPath<E> needle, IPath<E> haystack)
	{
		return findOverlappedPossiblePrefixes(needle, haystack, false);
	}

	static public <E> int[] findOverlappedPossiblePrefixes(IPath<E> needle, IPath<E> haystack, boolean firstFind)
	{
		int ret[] = findPossiblePrefixes(needle, haystack, firstFind, true);

		if (ret.length == 0)
			return ArrayUtils.EMPTY_INT_ARRAY;

		int i = ret.length - 1;

		if (ret[i] == needle.size() || ret[i] == haystack.size())
			ret = ArrayUtils.remove(ret, i);

		return ret;
	}

	static public <E> int[] findPossiblePrefixes(IPath<E> needle, IPath<E> haystack, boolean properSuffix)
	{
		return findPossiblePrefixes(needle, haystack, false, properSuffix);
	}

	static public <E> int[] findPossibleSuffixes(IPath<E> needle, IPath<E> haystack, boolean properSuffix)
	{
		return findPossibleSuffixes(needle, haystack, false, properSuffix);
	}

	/**
	 * Check if needle may be a prefix of haystack.
	 * Test if a suffix of needle is a prefix of haystack.
	 * The trivial prefix (needle == haystack) is not an answer.
	 * 
	 * @return The size of the founded prefixes.
	 */
	static public <E> int[] findPossiblePrefixes(IPath<E> needle, IPath<E> haystack, boolean findFirst, boolean properPrefix)
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
			int ret[] = findSimpleSuffixPrefix(needle, haystack, findFirst, false);

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

	static public <E> int[] findPossibleSuffixes(IPath<E> needle, IPath<E> haystack, boolean findFirst, boolean properSuffix)
	{
		return findPossiblePrefixes(haystack, needle, findFirst, properSuffix);
	}

	// =========================================================================
	// HAS SUFFIX <- in -> PREFIX
	// =========================================================================

	static public <E> boolean hasAllSimpleSuffixInPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return hasSimpleSuffixInPrefix(needle, haystack, false);
	}

	static public <E> boolean hasProperSimpleSuffixInPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return hasSimpleSuffixInPrefix(needle, haystack, true);
	}

	static public <E> boolean hasAllSimplePrefixInSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return hasSimplePrefixInSuffix(needle, haystack, false);
	}

	static public <E> boolean hasProperSimplePrefixInSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return hasSimplePrefixInSuffix(needle, haystack, true);
	}

	static public <E> boolean hasSimpleSuffixInPrefix(IPath<E> needle, IPath<E> haystack, boolean properPrefixSuffix)
	{
		return findSimpleSuffixPrefix(needle, haystack, true, properPrefixSuffix).length == 1;
	}

	static public <E> boolean hasSimplePrefixInSuffix(IPath<E> needle, IPath<E> haystack, boolean properPrefixSuffix)
	{
		return findSimplePrefixSuffix(needle, haystack, true, properPrefixSuffix).length == 1;
	}

	// =========================================================================
	// SUFFIX PREFIX
	// =========================================================================

	public static <E> int[] findAllSimpleSuffixPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return findSimpleSuffixPrefix(needle, haystack, false);
	}

	// Préfixes et suffixes stricts (limites exclues)
	public static <E> int[] findProperSimpleSuffixPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return findSimpleSuffixPrefix(needle, haystack, true);
	}

	public static <E> int[] findSimpleSuffixPrefix(IPath<E> needle, IPath<E> haystack, boolean properPrefixSuffix)
	{
		return findSimpleSuffixPrefix(needle, haystack, false, properPrefixSuffix);
	}

	private static <E> int[] findSimpleSuffixPrefix(IPath<E> needle, IPath<E> haystack, boolean findFirst, boolean properPrefixSuffix)
	{
		return findSimplePrefixSuffix(haystack, needle, false, properPrefixSuffix);
	}

	// =========================================================================
	// PREFIX SUFFIX
	// =========================================================================

	public static <E> int[] findAllSimplePrefixSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return findSimplePrefixSuffix(needle, haystack, false);
	}

	// Préfixes et suffixes stricts (limites exclues)
	public static <E> int[] findProperSimplePrefixSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return findSimplePrefixSuffix(needle, haystack, true);
	}

	public static <E> int[] findSimplePrefixSuffix(IPath<E> needle, IPath<E> haystack, boolean properPrefixSuffix)
	{
		return findSimplePrefixSuffix(needle, haystack, false, properPrefixSuffix);
	}

	private static <E> int[] findSimplePrefixSuffix(IPath<E> needle, IPath<E> haystack, boolean findFirst, boolean properPrefixSuffix)
	{
		List<E> n_labels = needle.getLabels();
		List<E> h_labels = haystack.getLabels();

		int n_size = needle.getLabels().size();
		int h_size = haystack.getLabels().size();
		int min;

		min = Math.min(h_size, n_size);

		if (!properPrefixSuffix)
			min++;

		ArrayList<Integer> array = new ArrayList<>(min);

		for (int len = 1; len < min; len++)
		{
			List<E> pref = n_labels.subList(0, len);
			List<E> suff = h_labels.subList(h_size - len, h_size);

			if (!pref.equals(suff))
				continue;

			array.add(len);

			if (findFirst)
				break;
		}
		return array.stream().mapToInt(Integer::intValue).toArray();
	}
}
