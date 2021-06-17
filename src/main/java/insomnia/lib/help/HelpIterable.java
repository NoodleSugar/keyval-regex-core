package insomnia.lib.help;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class HelpIterable
{
	private HelpIterable()
	{
		throw new AssertionError();
	}

	/**
	 * Check if each {@link Predicate} of a list validate an element of a sequence.
	 * 
	 * @param <E>        type of the elements to check
	 * @param sequence   the sequence of elements
	 * @param predicates the predicates
	 * @return {@code true} if each predicate validated at least an element of a sequence.
	 */
	public static <E> boolean matchPredicatesAtLeastOnce(Iterable<E> sequence, List<Predicate<E>> predicates)
	{
		var toCheck = new LinkedList<>(predicates);

		for (E element : sequence)
		{
			var piterator = toCheck.iterator();

			while (piterator.hasNext())
			{
				if (piterator.next().test(element))
				{
					piterator.remove();

					if (toCheck.isEmpty())
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if each {@link Predicate} of a list validate an element of a sequence.
	 * Each predicate must validate a different element than the previous predicate.
	 * 
	 * @param <E>        type of the elements to check
	 * @param sequence   the sequence of elements
	 * @param predicates the predicates
	 * @return {@code true} if each predicate validated at least an element of a sequence and that the element is a different one that for the other predicates.
	 */
	public static <E> boolean matchDisjointPredicatesAtLeastOnce(Iterable<E> sequence, List<Predicate<E>> predicates)
	{
		var toCheck = new LinkedList<>(predicates);

		for (E element : sequence)
		{
			var piterator = toCheck.iterator();

			while (piterator.hasNext())
			{
				if (piterator.next().test(element))
				{
					piterator.remove();

					if (toCheck.isEmpty())
						return true;

					break;
				}
			}
		}
		return false;
	}
}
