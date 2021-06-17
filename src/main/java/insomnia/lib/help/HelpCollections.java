package insomnia.lib.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;

public final class HelpCollections
{
	private HelpCollections()
	{
		throw new AssertionError();
	}

	public static <E, R extends Collection<E>> Collection<E> union(Iterable<? extends Iterable<E>> sets)
	{
		return union(sets, new ArrayList<>());
	}

	public static <E, R extends Collection<E>> Collection<E> union(Iterable<? extends Iterable<E>> sets, R outputCollection)
	{
		for (var set : sets)
			CollectionUtils.addAll(outputCollection, set);

		return outputCollection;
	}

	/**
	 * Naive algorithm for deduplicate elements with an equality {@link Predicate}.
	 * 
	 * @param <E>      type of elements
	 * @param elements the sequence of elements
	 * @param pequals  the predicate that must return {@code true} if two elements are equals
	 * @return a collection of deduplicate elements
	 */
	public static <E> Collection<E> unique(Iterable<? extends E> elements, BiPredicate<E, E> pequals)
	{
		return unique(elements, pequals, new ArrayList<>());
	}

	/**
	 * Naive algorithm for deduplicate elements with an equality {@link Predicate}.
	 * 
	 * @param <E>              type of elements
	 * @param <R>              type of the output collection
	 * @param elements         the sequence of elements
	 * @param pequals          the predicate that must return {@code true} if two elements are equals
	 * @param outputCollection the collection to fill and return
	 * @return the output collection with unique elements added
	 */
	public static <E, R extends Collection<E>> R unique(Iterable<? extends E> elements, BiPredicate<E, E> pequals, R outputCollection)
	{
		LinkedList<E> tmp = new LinkedList<>();
		CollectionUtils.addAll(tmp, elements);

		for (int pos = 0; pos < tmp.size();)
		{
			E   a         = tmp.get(pos);
			var iterator2 = tmp.listIterator(++pos);

			while (iterator2.hasNext())
			{
				E b = iterator2.next();
				if (pequals.test(a, b))
					iterator2.remove();
			}
			outputCollection.add(a);
		}
		return outputCollection;
	}
}
