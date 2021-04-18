package insomnia.fsa;

import java.util.Objects;

import insomnia.implem.fsa.valuecondition.FSAValueConditions;

public interface IFSAState<VAL, LBL>
{
	IFSAValueCondition<VAL> getValueCondition();

	IFSANodeCondition<VAL, LBL> getNodeCondition();

	// =========================================================================

	static <VAL, LBL> boolean areEqual(IFSAState<VAL, LBL> a, IFSAState<VAL, LBL> b)
	{
		if (a == b)
			return true;

//		var aNodeC = a.getNodeCondition();
		var aValueC = a.getValueCondition();
//		var bNodeC = b.getNodeCondition();
		var bValueC = b.getValueCondition();

		return Objects.equals(aValueC, bValueC);
	}

	static <VAL, LBL> boolean projectOn(IFSAState<VAL, LBL> a, IFSAState<VAL, LBL> b)
	{
		if (a == b)
			return true;

//		var aNodeC = a.getNodeCondition();
		var aValueC = a.getValueCondition();
//		var bNodeC = b.getNodeCondition();
		var bValueC = b.getValueCondition();

		return Objects.equals(aValueC, FSAValueConditions.createAny()) || //
			Objects.equals(aValueC, bValueC);
	}

	static <VAL, LBL> int compare(IFSAState<VAL, LBL> a, IFSAState<VAL, LBL> b)
	{
		var aValueC = a.getValueCondition();
		var bValueC = b.getValueCondition();

		if (a == b || Objects.equals(aValueC, bValueC))
			return 0;

		if (Objects.equals(aValueC, FSAValueConditions.createAny()))
			return -1;

		return 1;
	}

	static <VAL, LBL> IFSAState<VAL, LBL> getProjectionSource(IFSAState<VAL, LBL> a, IFSAState<VAL, LBL> b)
	{
		if (projectOn(a, b))
			return a;
		if (projectOn(b, a))
			return b;
		return null;
	}

	static <VAL, LBL> boolean hasProjection(IFSAState<VAL, LBL> a, IFSAState<VAL, LBL> b)
	{
		return null != getProjectionSource(a, b);
	}
}
