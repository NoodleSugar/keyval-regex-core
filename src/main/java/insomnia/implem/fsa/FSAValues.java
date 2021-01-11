package insomnia.implem.fsa;

import insomnia.fsa.IFSAValueCondition;

public final class FSAValues
{
	private FSAValues()
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
}
