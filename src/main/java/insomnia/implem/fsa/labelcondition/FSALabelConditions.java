package insomnia.implem.fsa.labelcondition;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import insomnia.fsa.IFSALabelCondition;

public final class FSALabelConditions
{
	private FSALabelConditions()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private final static IFSALabelCondition<?> trueCondition;
	private final static IFSALabelCondition<?> epsilonCondition;

	private abstract static class AbstractLabelCondition<LBL> implements IFSALabelCondition<LBL>
	{
		private Object obj;

		public AbstractLabelCondition(Object obj)
		{
			this.obj = obj;
		}

		@Override
		public boolean test()
		{
			return false;
		}

		@Override
		public Collection<LBL> getLabels()
		{
			return Collections.emptyList();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
				return true;
			if (!(obj instanceof AbstractLabelCondition))
				return false;

			return this.obj.equals(((AbstractLabelCondition<LBL>) obj).obj);
		}

		@Override
		public int hashCode()
		{
			return obj.hashCode();
		}

		@Override
		public String toString()
		{
			return obj.toString();
		}
	}

	static
	{
		trueCondition = new IFSALabelCondition<Object>()
		{
			@Override
			public boolean test(Object element)
			{
				return true;
			}

			@Override
			public boolean test()
			{
				return false;
			}

			@Override
			public Collection<Object> getLabels()
			{
				return Collections.emptyList();
			}

			@Override
			public String toString()
			{
				return "*";
			}

			@Override
			public boolean equals(Object obj)
			{
				return obj == this;
			}
		};

		epsilonCondition = new IFSALabelCondition<Object>()
		{
			@Override
			public boolean test(Object element)
			{
				return false;
			}

			@Override
			public boolean test()
			{
				return true;
			}

			@Override
			public Collection<Object> getLabels()
			{
				return Collections.emptyList();
			}

			@Override
			public String toString()
			{
				return "ε";
			}

			@Override
			public boolean equals(Object obj)
			{
				return obj == this;
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <LBL> IFSALabelCondition<LBL> trueCondition()
	{
		return (IFSALabelCondition<LBL>) trueCondition;
	}

	@SuppressWarnings("unchecked")
	public static <LBL> IFSALabelCondition<LBL> epsilonCondition()
	{
		return (IFSALabelCondition<LBL>) epsilonCondition;
	}

	public static <LBL> IFSALabelCondition<LBL> createEq(LBL label)
	{
		return new AbstractLabelCondition<LBL>(label)
		{
			@Override
			public boolean test(LBL element)
			{
				return element.equals(label);
			}

			@Override
			public Collection<LBL> getLabels()
			{
				return Collections.singleton(label);
			}
		};
	}

	public static <LBL> IFSALabelCondition<LBL> createRegex(String regex)
	{
		Pattern pattern = Pattern.compile(regex);

		return new AbstractLabelCondition<LBL>(regex)
		{
			@Override
			public boolean test(LBL element)
			{
				return pattern.matcher(element.toString()).matches();
			}
		};
	}

	public static boolean isTrueCondition(IFSALabelCondition<?> labelCondition)
	{
		return labelCondition == trueCondition;
	}

	public static boolean isEpsilonCondition(IFSALabelCondition<?> labelCondition)
	{
		return labelCondition.test();
	}

}
