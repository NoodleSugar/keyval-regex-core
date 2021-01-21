package insomnia.implem.fsa.valuecondition;

import java.util.Optional;

import insomnia.fsa.IFSAValueCondition;

public final class FSAValueConditions
{
	private FSAValueConditions()
	{
		throw new UnsupportedOperationException();
	}

	// =========================================================================

	private static class FSAValueAny<VAL> implements IFSAValueCondition<VAL>
	{
		@Override
		public boolean test(VAL element)
		{
			return true;
		}

		@Override
		public String toString()
		{
			return "*";
		}
	}

	static IFSAValueCondition<Object> any = new FSAValueAny<>();

	@SuppressWarnings("unchecked")
	public static <VAL> IFSAValueCondition<VAL> createAny()
	{
		return (IFSAValueCondition<VAL>) any;
	}

	// =========================================================================

	private static class FSAValueEq<VAL> implements IFSAValueCondition<VAL>
	{
		VAL value;

		public FSAValueEq(VAL value)
		{
			this.value = value;
		}

		@Override
		public boolean test(VAL element)
		{
			return value.equals(element);
		}

		@Override
		public String toString()
		{
			return "=" + value.toString();
		}
	}

	public static <VAL> IFSAValueCondition<VAL> createEq(VAL value)
	{
		return new FSAValueEq<VAL>(value);
	}

	/**
	 * Create any if value is absent, or eq if present.
	 */
	public static <VAL> IFSAValueCondition<VAL> createAnyOrEq(Optional<VAL> value)
	{
		if (value.isPresent())
			return createEq(value.get());

		return createAny();
	}
}
