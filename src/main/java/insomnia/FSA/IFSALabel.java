package insomnia.FSA;

import java.util.function.Predicate;

/**
 * Note: an epsilon transition evaluate test(null) = true.
 * 
 * @author zuri
 * @param <E>
 */
public interface IFSALabel<E> extends Predicate<E>
{
	@Override
	boolean test(E element);

	/**
	 * true if epsilon transition
	 */
	boolean test();
}
