package insomnia.implem.fsa.labelcondition;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import insomnia.fsa.IFSALabelCondition;

/**
 * A predicate base on Java {@link Predicate}.
 * 
 * @author zuri
 * @param <LBL>
 */
public final class PredicateFSALabelCondition<LBL> implements IFSALabelCondition<LBL>
{
	Predicate<LBL> predicate;
	Object         print;

	private PredicateFSALabelCondition(Predicate<LBL> predicate, Object print)
	{
		this.predicate = predicate;
		this.print     = print;
	}

	public static <LBL> PredicateFSALabelCondition<LBL> create(Predicate<LBL> predicate, Object print)
	{
		return new PredicateFSALabelCondition<LBL>(predicate, print);
	}

	@Override
	public boolean test(LBL element)
	{
		return predicate.test(element);
	}

	@Override
	public boolean test()
	{
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (!(obj instanceof PredicateFSALabelCondition))
			return false;

		return predicate.equals(((PredicateFSALabelCondition<LBL>) obj).predicate);
	}

	@Override
	public int hashCode()
	{
		return predicate.hashCode();
	}

	@Override
	public String toString()
	{
		return print.toString();
	}

	@Override
	public Collection<LBL> getLabels()
	{
		return Collections.emptyList();
	}
}
