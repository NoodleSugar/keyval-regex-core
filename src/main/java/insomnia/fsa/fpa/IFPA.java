package insomnia.fsa.fpa;

import java.util.function.Predicate;

import insomnia.data.IPath;
import insomnia.implem.fsa.fpa.IPath_as_IFPAElement;

/**
 * E : type of tested elements
 */
@FunctionalInterface
public interface IFPA<VAL, LBL> extends Predicate<IFPAPath<VAL, LBL>>
{
	/**
	 * @param elements to test
	 * @return true if it succeed
	 */
	@Override
	boolean test(IFPAPath<VAL, LBL> element);

	default boolean test(IPath<VAL, LBL> path)
	{
		return test(IPath_as_IFPAElement.get(path));
	}
}
