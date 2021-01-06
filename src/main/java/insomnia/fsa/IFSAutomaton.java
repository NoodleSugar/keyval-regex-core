package insomnia.fsa;

import java.util.function.Predicate;

import insomnia.data.IPath;

/**
 * E : type of tested elements
 */
@FunctionalInterface
public interface IFSAutomaton<VAL, LBL> extends Predicate<IFSAElement<VAL, LBL>>
{
	/**
	 * @param elements to test
	 * @return true if it succeed
	 */
	@Override
	boolean test(IFSAElement<VAL, LBL> element);

	default boolean test(IPath<VAL, LBL> path)
	{
		return test(IPath_as_IFSAElement.get(path));
	}
}
