package insomnia.implem.fsa.fpa.graphchunk;

import java.security.InvalidParameterException;

import insomnia.fsa.IFSAValueCondition;
import insomnia.implem.fsa.valuecondition.FSAValueConditions;

final class GCStates
{
	private GCStates()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static abstract class AbstractGCState<VAL, LBL> implements IGCState<VAL, LBL>
	{
		private boolean isInitial, isFinal, isRooted, isTerminal;

		private IFSAValueCondition<VAL> valueCondition;

		public AbstractGCState(IFSAValueCondition<VAL> valueCondition)
		{
			this.valueCondition = valueCondition;

			this.isInitial  = false;
			this.isFinal    = false;
			this.isRooted   = false;
			this.isTerminal = false;
		}

		@Override
		public IFSAValueCondition<VAL> getValueCondition()
		{
			return valueCondition;
		}

		AbstractGCState<VAL, LBL> setInitial(boolean isInitial)
		{
			this.isInitial = isInitial;
			return this;
		}

		AbstractGCState<VAL, LBL> setFinal(boolean isFinal)
		{
			this.isFinal = isFinal;
			return this;
		}

		AbstractGCState<VAL, LBL> setRooted(boolean isRooted)
		{
			this.isRooted = isRooted;
			return this;
		}

		AbstractGCState<VAL, LBL> setTerminal(boolean isTerminal)
		{
			this.isTerminal = isTerminal;
			return this;
		}

		@Override
		public boolean isInitial()
		{
			return isInitial;
		}

		@Override
		public boolean isFinal()
		{
			return isFinal;
		}

		@Override
		public boolean isRooted()
		{
			return isRooted;
		}

		@Override
		public boolean isTerminal()
		{
			return isTerminal;
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			sb.append("<[");

			if (isInitial)
				sb.append("I");

			if (isRooted)
				sb.append("^");

			sb.append(Integer.toHexString(System.identityHashCode(this)));

			if (!valueCondition.toString().isEmpty())
				sb.append(":").append(valueCondition.toString());

			if (isTerminal)
				sb.append("$");

			if (isFinal)
				sb.append("F");

			sb.append("]>");

			return sb.toString();
		}
	}

	// =========================================================================

	private static class StateSimple<VAL, LBL> extends AbstractGCState<VAL, LBL>
	{
		public StateSimple(IFSAValueCondition<VAL> valueCondition)
		{
			super(valueCondition);
		}
	}

	private static <VAL, LBL> IGCState<VAL, LBL> create(boolean isRooted, boolean isTerminal, boolean isInitial, boolean isFinal, IFSAValueCondition<VAL> valueCondition)
	{
		return new StateSimple<VAL, LBL>(valueCondition) //
			.setRooted(isRooted) //
			.setTerminal(isTerminal) //
			.setInitial(isInitial) //
			.setFinal(isFinal);
	}

	/**
	 * Create a ValueEq node if null != value.
	 * Else create a simple node (accept any value).
	 * 
	 * @return
	 */
	private static <VAL, LBL> IGCState<VAL, LBL> createNullableValueEq(boolean isRooted, boolean isTerminal, boolean isInitial, boolean isFinal, VAL value)
	{
		if (null == value)
			return create(isRooted, isTerminal, isInitial, isFinal, FSAValueConditions.createAny());

		return create(isRooted, isTerminal, isInitial, isFinal, FSAValueConditions.createEq(value));

	}

	public static <VAL, LBL> IGCState<VAL, LBL> create(VAL value)
	{
		return createNullableValueEq(false, false, false, false, value);
	}

	public static <VAL, LBL> IGCState<VAL, LBL> create()
	{
		return create(null);
	}

	// =========================================================================

	public static <VAL, LBL> void setRooted(IGCState<VAL, LBL> state)
	{
		setRooted(state, true);
	}

	public static <VAL, LBL> void setTerminal(IGCState<VAL, LBL> state)
	{
		setTerminal(state, true);
	}

	public static <VAL, LBL> void setInitial(IGCState<VAL, LBL> state)
	{
		setInitial(state, true);
	}

	public static <VAL, LBL> void setFinal(IGCState<VAL, LBL> state)
	{
		setFinal(state, true);
	}

	public static <VAL, LBL> void setRooted(IGCState<VAL, LBL> state, boolean isRooted)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL, LBL>) state).setRooted(isRooted);
	}

	public static <VAL, LBL> void setTerminal(IGCState<VAL, LBL> state, boolean isTerminal)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL, LBL>) state).setTerminal(isTerminal);
	}

	public static <VAL, LBL> void setInitial(IGCState<VAL, LBL> state, boolean isInitial)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL, LBL>) state).setInitial(isInitial);
	}

	public static <VAL, LBL> void setFinal(IGCState<VAL, LBL> state, boolean isFinal)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL, LBL>) state).setFinal(isFinal);
	}

	public static <VAL, LBL> void setValueCondition(IGCState<VAL, LBL> state, IFSAValueCondition<VAL> valueCondition)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL, LBL>) state).valueCondition = valueCondition;
	}

	public static <VAL, LBL> void merge(IGCState<VAL, LBL> dest, IGCState<VAL, LBL> src)
	{
		if (!(dest instanceof AbstractGCState))
			throw new InvalidParameterException();

		if (dest.getValueCondition().equals(FSAValueConditions.createAny()))
			GCStates.setValueCondition(dest, src.getValueCondition());
		else if (!src.getValueCondition().equals(dest.getValueCondition()))
			throw new AssertionError();

		if (src.isRooted())
			setRooted(dest, true);
		if (src.isTerminal())
			setTerminal(dest, true);
		if (src.isInitial())
			setInitial(dest, true);
		if (src.isFinal())
			setFinal(dest, true);
	}

	public static <VAL, LBL> void copy(IGCState<VAL, LBL> dest, IGCState<VAL, LBL> src)
	{
		if (!(dest instanceof AbstractGCState))
			throw new InvalidParameterException();

		AbstractGCState<VAL, LBL> gcDest = (AbstractGCState<VAL, LBL>) dest;
		gcDest.valueCondition = src.getValueCondition();
		setRooted(dest, src.isRooted());
		setTerminal(dest, src.isTerminal());
		setInitial(dest, src.isInitial());
		setFinal(dest, src.isFinal());
	}

	public static <VAL, LBL> IGCState<VAL, LBL> copy(IGCState<VAL, LBL> src)
	{
		return create(src.isRooted(), src.isTerminal(), src.isInitial(), src.isFinal(), src.getValueCondition());
	}
}
