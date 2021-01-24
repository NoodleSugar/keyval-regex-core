package insomnia.fsa;

import java.util.Collection;
import java.util.function.Predicate;

public interface IFSALabelCondition<LBL> extends Predicate<LBL>
{
	@Override
	boolean test(LBL element);

	/**
	 * true if epsilon transition
	 */
	boolean test();

	/**
	 * Get all the labels validated by the condition.
	 * 
	 * @return
	 */
	Collection<LBL> getLabels();
}
