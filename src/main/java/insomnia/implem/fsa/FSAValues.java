package insomnia.implem.fsa;

import insomnia.fsa.IFSAValueCondition;

public final class FSAValues
{
	private FSAValues()
	{

	}

	// =========================================================================

	static IFSAValueCondition<Object> any = new FSAValueAny<>();

	@SuppressWarnings("unchecked")
	public static <VAL> IFSAValueCondition<VAL> createAny()
	{
		return (IFSAValueCondition<VAL>) any;
	}

	// =========================================================================

	public static <VAL> IFSAValueCondition<VAL> createEq(VAL value)
	{
		return new FSAValueEq<VAL>(value);
	}
}
