package insomnia.implem.fsa.graphchunk;

public final class GCStates
{
	private GCStates()
	{
	}

	// =========================================================================

	static abstract class AbstractGCState<VAL> implements IGCState<VAL>
	{
		private int id;

		public AbstractGCState(int id)
		{
			this.id = id;
		}

		public int getId()
		{
			return id;
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
			return "<:" + id + ":>";
		}
	}

	// =========================================================================

	static class StateSimple<VAL> extends AbstractGCState<VAL>
	{
		public StateSimple(int id)
		{
			super(id);
		}

		@Override
		public boolean isTerminal()
		{
			return false;
		}

		@Override
		public boolean test(VAL t)
		{
			return t == null;
		}
	}

	public static <VAL> IGCState<VAL> createSimple(int id)
	{
		return new StateSimple<VAL>(id);
	}
	// =========================================================================

	static class StateTerminal<VAL> extends StateSimple<VAL>
	{
		public StateTerminal(int id)
		{
			super(id);
		}

		@Override
		public boolean isTerminal()
		{
			return true;
		}
	}

	public static <VAL> IGCState<VAL> createTerminal(int id)
	{
		return new StateSimple<VAL>(id);
	}

	// =========================================================================

	static class StateValueEq<VAL> extends AbstractGCState<VAL>
	{
		VAL value;

		public StateValueEq(int id, VAL value)
		{
			super(id);
		}

		@Override
		public boolean isTerminal()
		{
			return true;
		}

		@Override
		public boolean test(VAL t)
		{
			return t.equals(value);
		}
	}

	public static <VAL> IGCState<VAL> createValueEq(int id, VAL value)
	{
		return new StateValueEq<VAL>(id, value);
	}
}
