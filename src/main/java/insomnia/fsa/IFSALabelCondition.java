package insomnia.fsa;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public interface IFSALabelCondition<LBL> extends Predicate<LBL>
{
	@Override
	boolean test(LBL element);

	/**
	 * Get all the labels validated by the condition if available.
	 */
	Collection<LBL> getLabels();

	// =========================================================================

	public static boolean isEpsilon(IFSALabelCondition<?> labelCondition)
	{
		return labelCondition == null;
	}

	public static boolean equals(IFSALabelCondition<?> a, IFSALabelCondition<?> b)
	{
		return Objects.equals(a, b);
	}
}
