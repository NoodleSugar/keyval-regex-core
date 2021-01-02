package insomnia.implem.fsa.graphchunk;

import java.security.InvalidParameterException;

import insomnia.fsa.IFSAValueCondition;
import insomnia.implem.fsa.FSAValues;

public final class GCStates
{
	private GCStates()
	{
	}

	// =========================================================================

	public static abstract class AbstractGCState<VAL> implements IGCState<VAL>
	{
		private int     id;
		private boolean isTerminal;

		private IFSAValueCondition<VAL> valueCondition;

		public AbstractGCState(int id, boolean isTerminal, IFSAValueCondition<VAL> valueCondition)
		{
			this.id             = id;
			this.isTerminal     = isTerminal;
			this.valueCondition = valueCondition;
		}

		@Override
		public int getId()
		{
			return id;
		}

		@Override
		public IFSAValueCondition<VAL> getValueCondition()
		{
			return valueCondition;
		}

		void setTerminal(boolean isTerminal)
		{
			this.isTerminal = isTerminal;
		}

		@Override
		public boolean isTerminal()
		{
			return isTerminal;
		}

		@Override
		public boolean test(VAL element)
		{
			return getValueCondition().test(element);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof AbstractGCState))
				return false;

			return id == ((AbstractGCState<VAL>) obj).id;
		}

		@Override
		public int hashCode()
		{
			return id;
		}

		@Override
		public String toString()
		{
			return "<:" + id + valueCondition + ":>";
		}
	}

	// =========================================================================

	static class StateSimple<VAL> extends AbstractGCState<VAL>
	{
		public StateSimple(int id, boolean isTerminal, IFSAValueCondition<VAL> valueCondition)
		{
			super(id, isTerminal, valueCondition);
		}
	}

	public static <VAL> IGCState<VAL> create(int id, boolean isTerminal, IFSAValueCondition<VAL> valueCondition)
	{
		return new StateSimple<VAL>(id, false, valueCondition);
	}

	public static <VAL> IGCState<VAL> createSimple(int id)
	{
		return new StateSimple<VAL>(id, false, FSAValues.createAny());
	}

	public static <VAL> IGCState<VAL> createTerminal(int id)
	{
		return new StateSimple<VAL>(id, true, FSAValues.createAny());
	}

	public static <VAL> IGCState<VAL> createTerminalValueEq(int id, VAL value)
	{
		return new StateSimple<VAL>(id, true, FSAValues.createEq(value));
	}

	/**
	 * Create a ValueEq node if null != value.
	 * Else create a simple node (accept any value).
	 * 
	 * @param id
	 * @param isTerminal
	 * @param value
	 * @return
	 */
	public static <VAL> IGCState<VAL> createNullableValueEq(int id, boolean isTerminal, VAL value)
	{
		if (null == value)
			return create(id, isTerminal, FSAValues.createAny());

		return create(id, isTerminal, FSAValues.createEq(value));
	}

	// =========================================================================

//	public static <VAL> void setTerminal(IGCState<VAL> state)
//	{
//		if (!(state instanceof AbstractGCState))
//			throw new InvalidParameterException();
//
//		((AbstractGCState<VAL>) state).isTerminal = true;
//	}

	public static <VAL> void copy(IGCState<VAL> dest, IGCState<VAL> src, int newId)
	{
		if (!(src instanceof AbstractGCState))
			throw new InvalidParameterException();

		AbstractGCState<VAL> gcDest = (AbstractGCState<VAL>) dest;
		gcDest.id             = newId;
		gcDest.isTerminal     = src.isTerminal();
		gcDest.valueCondition = src.getValueCondition();
	}

	public static <VAL> IGCState<VAL> copy(IGCState<VAL> src, int newId)
	{
		return new StateSimple<>(newId, src.isTerminal(), src.getValueCondition());
	}
}
