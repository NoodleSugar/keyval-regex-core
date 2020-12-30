package insomnia.fsa;

import java.util.function.Predicate;

public interface IFSALabel<LBL> extends Predicate<LBL>
{
	@Override
	boolean test(LBL element);

	/**
	 * true if epsilon transition
	 */
	boolean test();
}
