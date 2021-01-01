package insomnia.fsa;

import java.util.function.Predicate;

public interface IFSALabelCondition<LBL> extends Predicate<LBL>
{
	@Override
	boolean test(LBL element);

	/**
	 * true if epsilon transition
	 */
	boolean test();
}
