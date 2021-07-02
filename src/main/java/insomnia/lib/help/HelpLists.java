package insomnia.lib.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
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
	public static <E> List<E> staticList(Collection<? extends E> src)
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

	// ==========================================================================

	public static <E> List<E> excludeAll(List<? extends E> list, Collection<? extends Integer> pos, boolean posAreSorted)
	{
		List<E> ret       = new ArrayList<>(list.size() - pos.size());
		int     nbPos     = pos.size();
		int[]   positions = new int[nbPos + 1];

		System.arraycopy(ArrayUtils.toPrimitive(pos.toArray(new Integer[0])), 0, positions, 0, pos.size());
		positions[nbPos] = list.size();

		if (!posAreSorted)
			Arrays.sort(positions);

		int p       = 0;
		int exclude = positions[p];

		for (int i = 0, c = list.size(); i < c; i++)
		{
			if (i == exclude)
			{
				exclude = positions[++p];
				continue;
			}
			ret.add(list.get(i));
		}
		return ret;
	}

	// ==========================================================================

	public static <E> List<E> getAll(List<? extends E> list, Iterable<? extends Integer> pos)
	{
		List<E> ret = new ArrayList<>();
		getAll(ret, list, pos);
		return ret;
	}

	public static <E> List<E> getAll(List<? extends E> list, Collection<? extends Integer> pos)
	{
		List<E> ret = new ArrayList<>(pos.size());
		getAll(ret, list, pos);
		return ret;
	}

	public static <E> void getAll(List<? super E> dest, List<? extends E> list, Iterable<? extends Integer> pos)
	{
		for (int i : pos)
			dest.add(list.get(i));
	}

	// ==========================================================================

	/**
	 * Check that each element of a sequence match a predicate with any other element.
	 * 
	 * @param <E>   type of element
	 * @param list  the sequence of elements
	 * @param match the matching {@link BiPredicate}
	 * @return true if each element match true with another one using the predicate.
	 */
	public static <E> boolean eachMatchAny(Iterable<E> list, BiPredicate<E, E> match)
	{
		LinkedList<E> cpy = new LinkedList<>();
		CollectionUtils.addAll(cpy, list);
		int i = cpy.size();

		while (i-- != 0)
		{
			E a = cpy.poll();

			for (E b : cpy)
			{
				if (!match.test(a, b))
					return false;
			}
			cpy.add(a);
		}
		return true;
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
		Base base  = Base.from(a.size(), b.size());
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

	public static <E> Iterable<Pair<E, E>> pairsIterable(Iterable<E> sets)
	{
		return IteratorUtils.asIterable(pairs(sets));
	}

	public static <E> Iterator<Pair<E, E>> pairs(Iterable<E> sets)
	{
		return new Iterator<Pair<E, E>>()
		{
			Iterator<E> it   = sets.iterator();
			E           last = it.next();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Pair<E, E> next()
			{
				Pair<E, E> ret = Pair.of(last, it.next());
				last = ret.getRight();
				return ret;
			}
		};
	}

	public static Stream<List<Integer>> ipowerSetAsStream(Iterable<?> sets)
	{
		return StreamSupport.stream(ipowerSetIterable(sets).spliterator(), false);
	}

	public static Iterable<List<Integer>> ipowerSetIterable(Iterable<?> sets)
	{
		return IteratorUtils.asIterable(ipowerSet(sets));
	}

	public static Iterator<List<Integer>> ipowerSet(Iterable<?> sets)
	{
		return ipowerSet(sets, true);
	}

	private static Iterator<List<Integer>> ipowerSet(Iterable<?> sets, boolean skipEmptySet)
	{
		return new Iterator<>()
		{
			private BitSet bits;
			private int    size;
			{
				size = IterableUtils.size(sets);
				bits = new BitSet(size + 1);

				if (skipEmptySet)
					bits.set(0);
			}

			@Override
			public boolean hasNext()
			{
				return !bits.get(size);
			}

			@Override
			public List<Integer> next()
			{
				List<Integer> ret = new ArrayList<>(size);

				for (int i = 0; i < size; i++)
				{
					if (bits.get(i))
						ret.add(i);
				}

				for (int i = 0; i <= size; i++)
				{
					if (bits.get(i))
						bits.clear(i);
					else
					{
						bits.set(i);
						break;
					}
				}
				return ret;
			}
		};
	}

	public static <E> Stream<List<E>> powerSetAsStream(Iterable<E> sets)
	{
		return StreamSupport.stream(powerSetIterable(sets).spliterator(), false);
	}

	public static <E> Iterable<List<E>> powerSetIterable(Iterable<E> sets)
	{
		return IteratorUtils.asIterable(powerSet(sets));
	}

	public static <E> Iterator<List<E>> powerSet(Iterable<E> sets)
	{
		return powerSet(sets, true);
	}

	private static <E> Iterator<List<E>> powerSet(Iterable<E> sets, boolean skipEmptySet)
	{
		return new Iterator<>()
		{
			private BitSet bits;
			private int    size;
			{
				size = IterableUtils.size(sets);
				bits = new BitSet(size + 1);

				if (skipEmptySet)
					bits.set(0);
			}

			@Override
			public boolean hasNext()
			{
				return !bits.get(size);
			}

			@Override
			public List<E> next()
			{
				List<E> ret = new ArrayList<>(size);

				for (int i = 0; i < size; i++)
				{
					if (bits.get(i))
						ret.add(IterableUtils.get(sets, i));
				}

				for (int i = 0; i <= size; i++)
				{
					if (bits.get(i))
						bits.clear(i);
					else
					{
						bits.set(i);
						break;
					}
				}
				return ret;
			}
		};
	}

	public static <E> Stream<List<E>> powerSetAsStream(Iterable<E> sets, int size)
	{
		return StreamSupport.stream(powerSetIterable(sets, size).spliterator(), false);
	}

	public static <E> Iterable<List<E>> powerSetIterable(Iterable<E> sets, int size)
	{
		return IteratorUtils.asIterable(powerSet(sets, size));
	}

	public static <E> Iterator<List<E>> powerSet(Iterable<E> sets, int size)
	{
		return new Iterator<>()
		{
			private BitSet bits;
			private int    itSize, retSize;
			{
				retSize = size;
				itSize  = IterableUtils.size(sets);
				bits    = new BitSet(itSize + 1);

				if (retSize > itSize)
					bits.set(itSize);
				else
				{
					for (int i = 0; i < retSize; i++)
						bits.set(i);
				}
			}

			@Override
			public boolean hasNext()
			{
				return !bits.get(itSize);
			}

			@Override
			public List<E> next()
			{
				List<E> ret = new ArrayList<>(retSize);

				for (int i = 0; i < itSize; i++)
				{
					if (bits.get(i))
						ret.add(IterableUtils.get(sets, i));
				}

				int set = retSize;
				do
				{
					for (int i = 0; i <= itSize; i++)
					{
						if (bits.get(i))
						{
							bits.clear(i);
							retSize--;
						}
						else
						{
							bits.set(i);
							retSize++;
							break;
						}
					}
				} while (set != retSize);
				return ret;
			}
		};
	}

	public static <E> Collection<List<E>> getClasses(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		return getClasses(sets, compare, new ArrayList<>());
	}

	@SuppressWarnings("unchecked")
	public static <E> Collection<List<E>> getClasses(Iterable<E> sets, BiPredicate<E, E> compare, Collection<? super List<E>> outputCollection)
	{
		CollectionUtils.addAll(outputCollection, classes(sets, compare));
		return (Collection<List<E>>) outputCollection;
	}

	public static <E> Iterator<List<E>> classes(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		return new Iterator<List<E>>()
		{
			private MapIterator<E, List<E>> classes = mapClasses(sets, compare);

			@Override
			public boolean hasNext()
			{
				return classes.hasNext();
			}

			@Override
			public List<E> next()
			{
				classes.next();
				return classes.getValue();
			}
		};
	}

	public static <E> ListValuedMap<E, E> getMapClasses(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		var                 it  = mapClasses(sets, compare);
		ListValuedMap<E, E> ret = new ArrayListValuedHashMap<>();

		while (it.hasNext())
		{
			var key = it.next();
			ret.putAll(key, it.getValue());
		}
		return ret;
	}

	public static <E> MapIterator<E, List<E>> mapClasses(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		List<E> elements = new ArrayList<>();
		CollectionUtils.addAll(elements, sets);
		int nbElements = elements.size();

		return new MapIterator<>()
		{
			int     processed = 0;
			E       key;
			List<E> values    = new ArrayList<>(nbElements);

			@Override
			public boolean hasNext()
			{
				return processed != nbElements;
			}

			@Override
			public E next()
			{
				key = elements.get(0);
				values.clear();

				for (E e : elements)
				{
					if (compare.test(key, e))
						values.add(e);
				}
				processed++;

				if (hasNext())
					Collections.swap(elements, 0, processed);

				return key;
			}

			@Override
			public E getKey()
			{
				return key;
			}

			@Override
			public List<E> getValue()
			{
				return List.copyOf(values);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public List<E> setValue(List<E> value)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public static <E> Collection<List<E>> getDisjointClasses(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		return IteratorUtils.toList(disjointClasses(sets, compare));
	}

	public static <E> Stream<List<E>> disjointClassesAsStream(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		return StreamSupport.stream(disjointClassesIterable(sets, compare).spliterator(), false);
	}

	public static <E> Iterable<List<E>> disjointClassesIterable(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		return IteratorUtils.asIterable(disjointClasses(sets, compare));
	}

	public static <E> Iterator<List<E>> disjointClasses(Iterable<E> sets, BiPredicate<E, E> compare)
	{
		Queue<E> elements = new LinkedList<>();
		CollectionUtils.addAll(elements, sets);

		return new Iterator<>()
		{
			@Override
			public boolean hasNext()
			{
				return !elements.isEmpty();
			}

			@Override
			public List<E> next()
			{
				List<E> ret     = new ArrayList<>();
				E       element = elements.poll();
				ret.add(element);
				var it = elements.iterator();

				while (it.hasNext())
				{
					E e = it.next();

					if (compare.test(element, e))
					{
						ret.add(e);
						it.remove();
					}
				}
				return ret;
			}

		};
	}

	public static <E> Collection<List<E>> getCartesianProduct(Iterable<? extends Iterable<E>> sets)
	{
		return IteratorUtils.toList(cartesianProduct(sets));
	}

	public static <E> Stream<List<E>> cartesianProductAsStream(Iterable<? extends Iterable<E>> sets)
	{
		return StreamSupport.stream(cartesianProductIterable(sets).spliterator(), false);
	}

	public static <E> Iterable<List<E>> cartesianProductIterable(Iterable<? extends Iterable<E>> sets)
	{
		return IteratorUtils.asIterable(cartesianProduct(sets));
	}

	public static <E> Iterator<List<E>> cartesianProduct(Iterable<? extends Iterable<E>> sets)
	{
		return new Iterator<List<E>>()
		{
			Base          base;
			int[]         num;
			int           i, nb;
			List<E>       ret;
			List<List<E>> ref;

			{
				i   = 0;
				nb  = IterableUtils.size(sets);
				ref = CollectionUtils.collect(sets, it -> IterableUtils.toList(it), new ArrayList<>(nb));
				num = new int[nb];
				ret = new ArrayList<>(nb);

				base = Base.cartesianProduct(sets);
			}

			@Override
			public boolean hasNext()
			{
				if (i >= base.size())
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
