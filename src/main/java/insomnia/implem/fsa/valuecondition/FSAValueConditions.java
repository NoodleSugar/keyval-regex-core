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

	// =========================================================================

	public static <VAL, LBL> IFSAValueCondition<VAL> union(IFSAValueCondition<VAL> a, IFSAValueCondition<VAL> b)
	{
		if (isAny(a) || Objects.equals(a, b))
			return a;
		if (isAny(b))
			return b;

		if (a instanceof FSAValueEq<?> || b instanceof FSAValueEq<?>)
		{
			FSAValueEq<VAL> aa = (FSAValueEq<VAL>) a;
			FSAValueEq<VAL> bb = (FSAValueEq<VAL>) b;

			if (Objects.equals(aa.value, bb.value))
				return a;
		}
		throw new IllegalArgumentException("Can't do the union of " + a + " with " + b);
	}

	public static <VAL, LBL> IFSAValueCondition<VAL> intersection(IFSAValueCondition<VAL> a, IFSAValueCondition<VAL> b)
	{
		if (isAny(a) || Objects.equals(a, b))
			return b;
		if (isAny(b))
			return a;

		if (a instanceof FSAValueEq<?> || b instanceof FSAValueEq<?>)
		{
			FSAValueEq<VAL> aa = (FSAValueEq<VAL>) a;
			FSAValueEq<VAL> bb = (FSAValueEq<VAL>) b;

			if (Objects.equals(aa.value, bb.value))
				return a;
		}
		throw new IllegalArgumentException("Can't do the intersection of " + a + " with " + b);
	}
}
