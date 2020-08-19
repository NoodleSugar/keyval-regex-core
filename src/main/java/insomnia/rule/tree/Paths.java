package insomnia.rule.tree;

import java.util.List;

import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.collections4.iterators.ReverseListIterator;

public final class Paths
{
	/*
	 * Search if needle is included in haystack
	 */
	static public <E> boolean isIncluded(IPath<E> needle, IPath<E> haystack)
	{
		List<E>   n_labels = needle.getLabels();
		List<E>   h_labels = haystack.getLabels();
		final int n_size   = needle.size();
		final int h_size   = haystack.size();

		if (n_size > h_labels.size())
			return false;

		/*
		 * The index is incremented each time the pair of labels are equals;
		 * and reset to 0 if not equals.
		 */
		int index = 0;

		for (int h_i = 0; h_i < h_size; h_i++)
		{
			E h_label = h_labels.get(h_i);

			if (!h_label.equals(n_labels.get(index)))
			{
				h_i   -= index;
				index  = 0;
				continue;
			}
			index++;

			/*
			 * All needle was consume
			 */
			if (index == n_size)
				return true;
		}
		return false;
	}

	/**
	 * Check if needle is prefix of haystack
	 */
	static public <E> boolean isPrefix(IPath<E> needle, IPath<E> haystack)
	{
		List<E>   n_labels = needle.getLabels();
		List<E>   h_labels = haystack.getLabels();
		final int n_size   = needle.size();

		if (n_size > haystack.size())
			return false;

		int index = 0;

		for (E h_label : h_labels)
		{
			if (!h_label.equals(n_labels.get(index)))
				return false;

			index++;

			if (index == n_size)
				return true;
		}
		return false;
	}

	static public <E> boolean isSuffix(IPath<E> needle, IPath<E> haystack)
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

	static public boolean areEquals(IPath<?> a, IPath<?> b)
	{
		return a.getLabels().equals(b.getLabels());
	}

	static public <E> boolean hasPrefixInSuffix(IPath<E> needle, IPath<E> haystack)
	{
		List<E> n_labels = needle.getLabels();
		List<E> h_labels = haystack.getLabels();

		int h_size = h_labels.size();

		int min = Math.min(h_size, n_labels.size());
		
		List<E> pref;
		List<E> suff;

		for (int i = 0 ; i < min ; i ++)
		{
			pref = n_labels.subList(0, i + 1);
			suff = h_labels.subList(h_size - 1 - i, h_size);
			
			if(pref.equals(suff))
				return true;
		}
		return false;
	}
}
