package insomnia.implem.fsa.valuecondition;

import java.util.Objects;

import insomnia.fsa.IFSAValueCondition;

public final class FSAValueConditions
{
	private FSAValueConditions()
	{
		throw new AssertionError();
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

	private static final IFSAValueCondition<Object> any = new FSAValueAny<>();

	@SuppressWarnings("unchecked")
	public static <VAL> IFSAValueCondition<VAL> createAny()
	{
		return (IFSAValueCondition<VAL>) any;
	}

	public static <VAL> boolean isAny(IFSAValueCondition<VAL> vcond)
	{
		return vcond == any;
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
			return Objects.equals(value, element);
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
				return true;
			if (!(obj instanceof FSAValueEq<?>))
				return false;

			@SuppressWarnings("unchecked")
			var veq = (FSAValueEq<VAL>) obj;
			return Objects.equals(value, veq.value);
		}

		@Override
		public String toString()
		{
			if (null == value)
				return "=`null`";

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
	public static <VAL> IFSAValueCondition<VAL> createAnyOrEq(VAL value)
	{
		if (null == value)
			return createAny();

		return createEq(value);
	}
}
