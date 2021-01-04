package insomnia.implem.fsa.graphchunk;

import java.security.InvalidParameterException;
import java.util.Optional;

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
		private boolean isInitial, isFinal, isRooted, isTerminal;

		private IFSAValueCondition<VAL> valueCondition;

		public AbstractGCState(int id, IFSAValueCondition<VAL> valueCondition)
		{
			this.id             = id;
			this.valueCondition = valueCondition;

			this.isInitial  = false;
			this.isFinal    = false;
			this.isRooted   = false;
			this.isTerminal = false;
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

		AbstractGCState<VAL> setInitial(boolean isInitial)
		{
			this.isInitial = isInitial;
			return this;
		}

		AbstractGCState<VAL> setFinal(boolean isFinal)
		{
			this.isFinal = isFinal;
			return this;
		}

		AbstractGCState<VAL> setRooted(boolean isRooted)
		{
			this.isRooted = isRooted;
			return this;
		}

		AbstractGCState<VAL> setTerminal(boolean isTerminal)
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
			StringBuilder sb = new StringBuilder();

			sb.append("<[");

			if (isInitial)
				sb.append("I");

			if (isRooted)
				sb.append("^");

			sb.append(id);

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

	static class StateSimple<VAL> extends AbstractGCState<VAL>
	{
		public StateSimple(int id, IFSAValueCondition<VAL> valueCondition)
		{
			super(id, valueCondition);
		}
	}

	private static <VAL> IGCState<VAL> create(int id, boolean isRooted, boolean isTerminal, boolean isInitial, boolean isFinal, IFSAValueCondition<VAL> valueCondition)
	{
		return new StateSimple<VAL>(id, valueCondition) //
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
	private static <VAL> IGCState<VAL> createNullableValueEq(int id, boolean isRooted, boolean isTerminal, boolean isInitial, boolean isFinal, VAL value)
	{
		if (null == value)
			return create(id, isRooted, isTerminal, isInitial, isFinal, FSAValues.createAny());

		return create(id, isRooted, isTerminal, isInitial, isFinal, FSAValues.createEq(value));

	}

	public static <VAL> IGCState<VAL> create(int id, Optional<VAL> value)
	{
		return createNullableValueEq(id, false, false, false, false, value.orElse(null));
	}

	public static <VAL> IGCState<VAL> create(int id)
	{
		return create(id, Optional.empty());
	}

	// =========================================================================

	public static <VAL> void setRooted(IGCState<VAL> state)
	{
		setRooted(state, true);
	}

	public static <VAL> void setTerminal(IGCState<VAL> state)
	{
		setTerminal(state, true);
	}

	public static <VAL> void setInitial(IGCState<VAL> state)
	{
		setInitial(state, true);
	}

	public static <VAL> void setFinal(IGCState<VAL> state)
	{
		setFinal(state, true);
	}

	public static <VAL> void setRooted(IGCState<VAL> state, boolean isRooted)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL>) state).setRooted(isRooted);
	}

	public static <VAL> void setTerminal(IGCState<VAL> state, boolean isTerminal)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL>) state).setTerminal(isTerminal);
	}

	public static <VAL> void setInitial(IGCState<VAL> state, boolean isInitial)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL>) state).setInitial(isInitial);
	}

	public static <VAL> void setFinal(IGCState<VAL> state, boolean isFinal)
	{
		if (!(state instanceof AbstractGCState))
			throw new InvalidParameterException();

		((AbstractGCState<VAL>) state).setFinal(isFinal);
	}

	public static <VAL> void copy(IGCState<VAL> dest, IGCState<VAL> src, int newId)
	{
		if (!(dest instanceof AbstractGCState))
			throw new InvalidParameterException();

		AbstractGCState<VAL> gcDest = (AbstractGCState<VAL>) dest;
		gcDest.id             = newId;
		gcDest.valueCondition = src.getValueCondition();
		setRooted(dest, src.isRooted());
		setTerminal(dest, src.isTerminal());
		setInitial(dest, src.isInitial());
		setFinal(dest, src.isFinal());
	}

	public static <VAL> IGCState<VAL> copy(IGCState<VAL> src, int newId)
	{
		return create(newId, src.isRooted(), src.isTerminal(), src.isInitial(), src.isFinal(), src.getValueCondition());
	}
}
