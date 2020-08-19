package insomnia.rule.tree;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.collections4.iterators.ReverseListIterator;

public final class Paths
{

	/**
	 * Search if needle is included in haystack but not equals.
	 */
	static public <E> boolean isIncluded(IPath<E> needle, IPath<E> haystack)
	{
		return findAllInclusions(needle, haystack, true).length == 1;
	}

	public static <E> int[] findAllInclusions(IPath<E> needle, IPath<E> haystack)
	{
		return findAllInclusions(needle, haystack, false);
	}

	private static <E> int[] findAllInclusions(IPath<E> needle, IPath<E> haystack, boolean firstFind)
	{
		if (needle.isRooted())
		{
			if (isPrefix(needle, haystack))
				return new int[] { 0 };

			return new int[0];
		}

		if (needle.isTerminal())
		{
			if (isSuffix(needle, haystack))
				return new int[] { haystack.size() - needle.size() };

			return new int[0];
		}
		return findAllSimpleInclusions(needle, haystack);
	}

	/**
	 * Search all inclusions of needle in haystack but not the equal inclusion.
	 */
	public static <E> int[] findAllSimpleInclusions(IPath<E> needle, IPath<E> haystack)
	{
		return findAllSimpleInclusions(needle, haystack, false);
	}

	private static <E> int[] findAllSimpleInclusions(IPath<E> needle, IPath<E> haystack, boolean firstFind)
	{
		List<E>   n_labels = needle.getLabels();
		List<E>   h_labels = haystack.getLabels();
		final int n_size   = needle.size();
		final int h_size   = haystack.size();

		ArrayList<Integer> ret = new ArrayList<>();

		if (n_size >= h_size)
			return new int[0];

		int h_i      = 1;
		int h_offset = 1;
		int n_i      = 0;

		/*
		 * No need to check if offset > h_offset_max because n_size become lower than the rest.
		 */
		final int h_offset_max = h_size - n_size;

		for (; h_i < h_size; h_i++)
		{
			E h_label = h_labels.get(h_i);
			E n_label = n_labels.get(n_i);

			/**
			 * Missmatch
			 */
			if (!h_label.equals(n_label))
			{
				n_i = 0;
				h_i = h_offset;
				h_offset++;

				if (h_offset >= h_offset_max)
					break;

				continue;
			}
			n_i++;

			/**
			 * Found one
			 */
			if (n_i == n_size)
			{
				ret.add(h_offset);

				if (firstFind)
					break;

				n_i = 0;
				h_i = h_offset;
				h_offset++;

				if (h_offset >= h_offset_max)
					break;
			}
		}
		return ret.stream().mapToInt(Integer::intValue).toArray();
	}

	static public <E> boolean isPrefix(IPath<E> needle, IPath<E> haystack)
	{
		if (needle.isTerminal())
			return false;

		if (haystack.isRooted())
		{
			if (!needle.isRooted())
				return false;
		}
		return isSimplePrefix(needle, haystack);
	}

	static public <E> boolean isSuffix(IPath<E> needle, IPath<E> haystack)
	{
		if (needle.isRooted())
			return false;

		if (haystack.isTerminal())
		{
			if (!needle.isTerminal())
				return false;
		}
		return isSimpleSuffix(needle, haystack);
	}

	/**
	 * Check if needle is prefix of haystack.
	 * The method do not take care of the isRooted aspects.
	 */
	static public <E> boolean isSimplePrefix(IPath<E> needle, IPath<E> haystack)
	{
		List<E>   n_labels = needle.getLabels();
		List<E>   h_labels = haystack.getLabels();
		final int n_size   = needle.size();
		final int h_size   = haystack.size();

		if (n_size >= h_size)
			return false;

		int index = 0;

		while (index < h_size)
		{
			E h_label = h_labels.get(index);
			E n_label = n_labels.get(index);

			if (!h_label.equals(n_label))
				return false;

			index++;

			if (index == n_size)
				return true;
		}
		return false;
	}

	/**
	 * Check if needle is suffix of haystack.
	 * The method do not take care of the isTerminal aspects.
	 */
	static public <E> boolean isSimpleSuffix(IPath<E> needle, IPath<E> haystack)
	{
		List<E>   n_labels = needle.getLabels();
		List<E>   h_labels = haystack.getLabels();
		final int n_size   = needle.size();

		if (n_size > haystack.size())
			return false;

		int index = n_size - 1;

		Iterable<E> it = new IteratorIterable<>(new ReverseListIterator<>(h_labels));

		for (E h_label : it)
		{
			if (!h_label.equals(n_labels.get(index)))
				return false;

			if (index == 0)
				return true;

			index--;
		}
		return false;
	}

	/**
	 * Check if needle may be a prefix of haystack considering haystack non rooted and non complete.
	 * Test if a suffix of needle is a prefix of haystack.
	 * The trivial prefix (needle == haystack) is not an answer.
	 */
	static public <E> int[] findAllPossiblePrefixes(IPath<E> needle, IPath<E> haystack)
	{
		if (haystack.isRooted())
		{
			if (needle.isRooted())
				return isSimplePrefix(needle, haystack) //
					? new int[] { needle.size() } //
					: new int[0];

			return new int[0];
		}
		return findAllSimpleSuffixPrefix(needle, haystack);
	}

	static public <E> int[] findAllPossibleSuffixes(IPath<E> needle, IPath<E> haystack)
	{
		if (haystack.isTerminal())
		{
			if (needle.isTerminal())
				return isSimpleSuffix(needle, haystack) //
					? new int[] { needle.size() } //
					: new int[0];

			return new int[0];
		}
		return findAllSimplePrefixSuffix(needle, haystack);
	}

	static public boolean areSimplyEquals(IPath<?> a, IPath<?> b)
	{
		return a.getLabels().equals(b.getLabels());
	}

	static public <E> boolean hasSimpleSuffixInPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return findAllSimpleSuffixPrefix(needle, haystack, true).length == 1;
	}

	static public <E> boolean hasSimplePrefixInSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return findAllSimplePrefixSuffix(needle, haystack, true).length == 1;
	}

	public static <E> int[] findAllSimpleSuffixPrefix(IPath<E> needle, IPath<E> haystack)
	{
		return findAllSimpleSuffixPrefix(needle, haystack, false);
	}

	private static <E> int[] findAllSimpleSuffixPrefix(IPath<E> needle, IPath<E> haystack, boolean findFirst)
	{
		return findAllSimplePrefixSuffix(haystack, needle, findFirst);
	}

	// Préfixes et suffixes stricts (limites exclues)
	public static <E> int[] findAllSimplePrefixSuffix(IPath<E> needle, IPath<E> haystack)
	{
		return findAllSimplePrefixSuffix(needle, haystack, false);
	}

	private static <E> int[] findAllSimplePrefixSuffix(IPath<E> needle, IPath<E> haystack, boolean findFirst)
	{
		List<E> n_labels = needle.getLabels();
		List<E> h_labels = haystack.getLabels();

		int n_size = needle.size();
		int h_size = haystack.size();
		int min;

		if (n_size == h_size)
			min = h_size;
		else
			min = Math.min(n_size, h_size) + 1;

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
