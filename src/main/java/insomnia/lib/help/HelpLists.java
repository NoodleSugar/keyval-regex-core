package insomnia.lib.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import insomnia.lib.numeric.Base;

public final class HelpLists
{
	/**
	 * Create a static list of size elements.
	 * Elements of its list may be modified but its not allowed to add/remove elements.
	 * 
	 * @param size the size of the list
	 * @return a static list of 'size' elements
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<E> staticList(int size)
	{
		return (List<E>) Arrays.asList(new Object[size]);
	}

	/**
	 * Transform a list to a static one.
	 * Elements of its list may be modified but its not allowed to add/remove elements.
	 * 
	 * @param src the list to copy
	 * @return a static copy of 'src'
	 */
	public static <E> List<E> staticList(List<? extends E> src)
	{
		ArrayList<E> ret = new ArrayList<>(src);
		ret.trimToSize();
		return ListUtils.fixedSizeList(ret);
	}

	/**
	 * @return an immutable list
	 */
	public static <E> List<E> downcast(List<? extends E> list)
	{
		return Collections.unmodifiableList(list);
	}

	public static <A, B> List<Object[]> mergePairsArrays(Collection<Pair<A[], B[]>> pairs)
	{
		if (pairs.size() == 0)
			return java.util.Collections.emptyList();

		List<Object[]> ret = new ArrayList<>(pairs.size());

		for (Pair<A[], B[]> pair : pairs)
			ret.add(ArrayUtils.addAll(pair.getLeft(), pair.getRight()));

		return ret;
	}

	public static List<List<Object>> mergePairsContent(Collection<Pair<? extends Collection<?>, ? extends Collection<?>>> pairs)
	{
		if (pairs.size() == 0)
			return java.util.Collections.emptyList();

		List<List<Object>> ret = new ArrayList<>(pairs.size());

		for (Pair<? extends Collection<?>, ? extends Collection<?>> pair : pairs)
			ret.add(mergePairContent(pair));

		return ret;
	}

	private static List<Object> mergePairContent(Pair<? extends Collection<?>, ? extends Collection<?>> pair)
	{
		List<Object> ret = new ArrayList<Object>();
		ret.addAll(pair.getLeft());
		ret.addAll(pair.getRight());
		return ret;
	}

	public static <A, B> List<Pair<A, B>> product(List<A> a, List<B> b)
	{
		int  nb    = a.size() * b.size();
		Base base  = new Base(a.size(), b.size());
		int  num[] = new int[2];
		Arrays.fill(num, 0);

		List<Pair<A, B>> ret = new ArrayList<>(nb);

		for (int i = 0; i < nb; i++)
		{
			ret.add(Pair.of(a.get(num[0]), b.get(num[1])));
			base.increment(num);
		}
		return ret;
	}

	public static <E> Iterator<List<E>> cartesianProduct(Collection<Collection<E>> sets)
	{
		return cartesianProduct(new ArrayList<>(sets));
	}

	public static <E> Iterator<List<E>> cartesianProduct(List<Collection<E>> sets)
	{
		return new Iterator<List<E>>()
		{
			Base          base;
			int[]         num;
			int           i, nb;
			List<E>       ret;
			List<List<E>> ref;

			{
				i  = 0;
				nb = sets.size();
				int ibase[] = new int[nb];
				int i       = 0;

				for (Collection<E> set : sets)
					ibase[i++] = set.size();

				ref = sets.stream().map(l -> l instanceof List ? (List<E>) l : new ArrayList<>(l)).collect(Collectors.toList());
				num = new int[nb];
				ret = new ArrayList<>(nb);

				base = new Base(ibase);
			}

			@Override
			public boolean hasNext()
			{
				if (i >= base.max())
					return false;

				i++;
				ret.clear();

				for (int j = 0; j < nb; j++)
					ret.add(ref.get(j).get(num[j]));

				base.increment(num);
				return true;
			}

			@Override
			public List<E> next()
			{
				return new ArrayList<>(ret);
			}
		};
	}

	// =========================================================================
	// INCLUSION SEARCH

	/**
	 * Find all the inclusions of needle in haystack.
	 * needle can't be a suffix or a prefix of haystack
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 * @return positions of each inclusion
	 */
	public static int[] findNoPrefSuffInclusions(List<?> needle, List<?> haystack)
	{
		return findInclusions(needle, haystack, true);
	}

	/**
	 * Find all the inclusions of needle in haystack.
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 * @return positions of each inclusion
	 */
	public static int[] findAllInclusions(List<?> needle, List<?> haystack)
	{
		return findInclusions(needle, haystack, false);
	}

	/**
	 * Find all the inclusions of needle in haystack.
	 * 
	 * @param needle           The one to search for
	 * @param haystack         The one to search in
	 * @param noSuffixOrPrefix needle can't be a suffix or a prefix of haystack
	 * @return positions of each inclusion
	 */
	public static int[] findInclusions(List<?> needle, List<?> haystack, boolean noSuffixOrPrefix)
	{
		return findInclusions(needle, haystack, false, noSuffixOrPrefix);
	}

	/**
	 * Find all the inclusions of needle in haystack.
	 * 
	 * @param needle           The one to search for
	 * @param haystack         The one to search in
	 * @param firstFind        Stop the process at the first inclusion founded
	 * @param noSuffixOrPrefix needle can't be a suffix or a prefix of haystack
	 * @return positions of each inclusion
	 */
	public static int[] findInclusions(List<?> needle, List<?> haystack, boolean firstFind, boolean noSuffixOrPrefix)
	{
		final int n_size = needle.size();
		final int h_size = haystack.size();

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
			List<?> h_part = haystack.subList(h_i, h_i + n_size);

			if (h_part.equals(needle))
			{
				ret.add(h_i);

				if (firstFind)
					break;
			}
		}
		return ret.stream().mapToInt(Integer::intValue).toArray();
	}
	// =========================================================================
	// PREFIX/SUFFIX SEARCH

	/**
	 * Is needle a prefix ({@code !needle.equals(haystack)}) of haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isPrefix(List<?> needle, List<?> haystack)
	{
		return isPrefix(needle, haystack, false);
	}

	/**
	 * Is needle a proper prefix ({@code !needle.equals(haystack)}) of haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isProperPrefix(List<?> needle, List<?> haystack)
	{
		return isPrefix(needle, haystack, true);
	}

	/**
	 * Is needle a suffix ({@code !needle.equals(haystack)}) of haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isSuffix(List<?> needle, List<?> haystack)
	{
		return isSuffix(needle, haystack, false);
	}

	/**
	 * Is needle a proper suffix ({@code !needle.equals(haystack)}) of haystack?
	 * 
	 * @param needle   The one to search for
	 * @param haystack The one to search in
	 */
	static public boolean isProperSuffix(List<?> needle, List<?> haystack)
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
	static public boolean isPrefix(List<?> needle, List<?> haystack, boolean properPrefix)
	{
		final int n_size = needle.size();
		final int h_size = haystack.size();

		if (properPrefix && n_size >= h_size //
			|| !properPrefix && n_size > h_size //
		)
			return false;

		return needle.equals(haystack.subList(0, n_size));
	}

	/**
	 * Is needle a suffix of haystack?
	 * 
	 * @param needle       The one to search for
	 * @param haystack     The one to search in
	 * @param properPrefix needle must be a proper suffix of haystack, that is {@code !needle.equals(haystack)}
	 */
	static public boolean isSuffix(List<?> needle, List<?> haystack, boolean properSuffix)
	{
		final int n_size = needle.size();
		final int h_size = haystack.size();

		if (properSuffix && n_size >= h_size //
			|| !properSuffix && n_size > h_size //
		)
			return false;

		return needle.equals(haystack.subList(h_size - n_size, h_size));
	}

	// =========================================================================
	// HAS SUFFIX <- in -> PREFIX
	// =========================================================================

	/**
	 * Does needle have a suffix that is a prefix of haystack?
	 * 
	 * @param needle   The one to search for a suffix
	 * @param haystack The one to search for a prefix
	 */
	static public boolean hasSuffixInPrefix(List<?> needle, List<?> haystack)
	{
		return hasSuffixInPrefix(needle, haystack, false);
	}

	/**
	 * Does needle have a suffix that is a prefix of haystack?
	 * needle can't be a prefix of haystack and haystack can't be a suffix of needle.
	 * 
	 * @param needle   The one to search for a suffix
	 * @param haystack The one to search for a prefix
	 */
	static public boolean hasProperSuffixInPrefix(List<?> needle, List<?> haystack)
	{
		return hasSuffixInPrefix(needle, haystack, true);
	}

	/**
	 * Does needle have a suffix that is a prefix of haystack?
	 * 
	 * @param needle             The one to search for a suffix
	 * @param haystack           The one to search for a prefix
	 * @param properPrefixSuffix needle can't be a prefix of haystack and haystack can't be a suffix of needle
	 */
	static public boolean hasSuffixInPrefix(List<?> needle, List<?> haystack, boolean properPrefixSuffix)
	{
		return findSuffixPrefix(needle, haystack, true, properPrefixSuffix).length == 1;
	}

	// =========================================================================

	/**
	 * Does needle have a prefix that is a suffix of haystack?
	 * 
	 * @param needle             The one to search for a prefix
	 * @param haystack           The one to search for a suffix
	 * @param properPrefixSuffix needle can't be a suffix of haystack and haystack can't be a prefix of needle
	 */
	static public boolean hasPrefixInSuffix(List<?> needle, List<?> haystack, boolean properPrefixSuffix)
	{
		return findPrefixSuffix(needle, haystack, true, properPrefixSuffix).length == 1;
	}

	/**
	 * Does needle have a prefix that is a suffix of haystack?
	 * 
	 * @param needle   The one to search for a prefix
	 * @param haystack The one to search for a suffix
	 * @return The sizes of prefixes
	 */
	static public boolean hasPrefixInSuffix(List<?> needle, List<?> haystack)
	{
		return hasPrefixInSuffix(needle, haystack, false);
	}

	/**
	 * Does needle have a prefix that is a suffix of haystack?
	 * needle can't be a suffix of haystack and haystack can't be a prefix of needle.
	 * 
	 * @param needle   The one to search for a prefix
	 * @param haystack The one to search for a suffix
	 * @return The sizes of prefixes
	 */
	static public boolean hasProperPrefixInSuffix(List<?> needle, List<?> haystack)
	{
		return hasPrefixInSuffix(needle, haystack, true);
	}

	// =========================================================================
	// SUFFIX PREFIX
	// =========================================================================

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * 
	 * @param needle   The one to search for suffixes
	 * @param haystack The one to search for prefixes
	 * @return The sizes of suffixes
	 */
	public static int[] findAllSuffixPrefix(List<?> needle, List<?> haystack)
	{
		return findSuffixPrefix(needle, haystack, false);
	}

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * needle can't be a prefix of haystack and haystack can't be a suffix of needle.
	 * 
	 * @param needle   The one to search for suffixes
	 * @param haystack The one to search for prefixes
	 * @return The sizes of suffixes
	 */
	public static int[] findProperSuffixPrefix(List<?> needle, List<?> haystack)
	{
		return findSuffixPrefix(needle, haystack, true);
	}

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * 
	 * @param needle             The one to search for suffixes
	 * @param haystack           The one to search for prefixes
	 * @param properPrefixSuffix needle can't be a prefix of haystack and haystack can't be a suffix of needle
	 * @return The sizes of suffixes
	 */
	public static int[] findSuffixPrefix(List<?> needle, List<?> haystack, boolean properPrefixSuffix)
	{
		return findSuffixPrefix(needle, haystack, false, properPrefixSuffix);
	}

	/**
	 * Fin all suffixes of needle that are prefixes of haystack.
	 * 
	 * @param needle             The one to search for suffixes
	 * @param haystack           The one to search for prefixes
	 * @param firstFind          Stop the process at the first inclusion founded
	 * @param properPrefixSuffix needle can't be a prefix of haystack and haystack can't be a suffix of needle
	 * @return The sizes of suffixes
	 */
	public static int[] findSuffixPrefix(List<?> needle, List<?> haystack, boolean firstFind, boolean properPrefixSuffix)
	{
		return findPrefixSuffix(haystack, needle, firstFind, properPrefixSuffix);
	}

	// =========================================================================
	// PREFIX SUFFIX
	// =========================================================================

	/**
	 * Fin all prefixes of needle that are suffixes of haystack.
	 * 
	 * @param needle   The one to search for prefixes
	 * @param haystack The one to search for suffixes
	 * @return The sizes of prefixes
	 */
	public static int[] findAllPrefixSuffix(List<?> needle, List<?> haystack)
	{
		return findPrefixSuffix(needle, haystack, false);
	}

	/**
	 * Fin all prefixes of needle that are suffixes of haystack.
	 * needle can't be a suffix of haystack and haystack can't be a prefix of needle.
	 * 
	 * @param needle   The one to search for prefixes
	 * @param haystack The one to search for suffixes
	 * @return The sizes of prefixes
	 */
	public static int[] findProperPrefixSuffix(List<?> needle, List<?> haystack)
	{
		return findPrefixSuffix(needle, haystack, true);
	}

	/**
	 * Fin all prefixes of needle that are suffixes of haystack.
	 * 
	 * @param needle             The one to search for prefixes
	 * @param haystack           The one to search for suffixes
	 * @param properPrefixSuffix needle can't be a suffix of haystack and haystack can't be a prefix of needle
	 * @return The sizes of prefixes
	 */
	public static int[] findPrefixSuffix(List<?> needle, List<?> haystack, boolean properPrefixSuffix)
	{
		return findPrefixSuffix(needle, haystack, false, properPrefixSuffix);
	}

	/**
	 * Fin all prefixes of needle that are suffixes of haystack.
	 * 
	 * @param needle             The one to search for prefixes
	 * @param haystack           The one to search for suffixes
	 * @param firstFind          Stop the process at the first inclusion founded
	 * @param properPrefixSuffix needle can't be a suffix of haystack and haystack can't be a prefix of needle
	 * @return The sizes of prefixes
	 */
	public static int[] findPrefixSuffix(List<?> needle, List<?> haystack, boolean firstFind, boolean properPrefixSuffix)
	{
		int n_size = needle.size();
		int h_size = haystack.size();
		int min;

		min = Math.min(h_size, n_size);

		if (!properPrefixSuffix)
			min++;

		ArrayList<Integer> array = new ArrayList<>(min);

		for (int len = 1; len < min; len++)
		{
			List<?> pref = needle.subList(0, len);
			List<?> suff = haystack.subList(h_size - len, h_size);

			if (!pref.equals(suff))
				continue;

			array.add(len);

			if (firstFind)
				break;
		}
		return array.stream().mapToInt(Integer::intValue).toArray();
	}
}
