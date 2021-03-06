package insomnia.fsa;

import java.util.Collection;
import java.util.function.Predicate;

public interface IFSAValueCondition<VAL> extends Predicate<VAL>
{
	@Override
	boolean test(VAL element);

	Collection<VAL> getValues();
}
