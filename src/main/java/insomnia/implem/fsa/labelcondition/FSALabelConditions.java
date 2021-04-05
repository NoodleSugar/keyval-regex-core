package insomnia.implem.fsa.labelcondition;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

import insomnia.fsa.IFSALabelCondition;

public final class FSALabelConditions
{
	private FSALabelConditions()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private final static IFSALabelCondition<?> any, anyLoop;

	private abstract static class AbstractLabelCondition<LBL> implements IFSALabelCondition<LBL>
	{
		private Object obj;

		public AbstractLabelCondition(Object obj)
		{
			this.obj = obj;
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

			return Objects.equals(this.obj, ((AbstractLabelCondition<LBL>) obj).obj);
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(obj);
		}

		@Override
		public String toString()
		{
			return Objects.toString(obj);
		}
	}

	private static abstract class Any<LBL> implements IFSALabelCondition<LBL>
	{
		@Override
		public boolean test(Object element)
		{
			return true;
		}

		@Override
		public Collection<LBL> getLabels()
		{
			return Collections.emptyList();
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj == this;
		}
	}

	static
	{
		any = new Any<>()
		{
			@Override
			public String toString()
			{
				return "*";
			}
		};

		anyLoop = new Any<>()
		{
			@Override
			public String toString()
			{
				return "[^$]*";
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <LBL> IFSALabelCondition<LBL> createAny()
	{
		return (IFSALabelCondition<LBL>) any;
	}

	@SuppressWarnings("unchecked")
	public static <LBL> IFSALabelCondition<LBL> createAnyLoop()
	{
		return (IFSALabelCondition<LBL>) anyLoop;
	}

	public static <LBL> IFSALabelCondition<LBL> epsilonCondition()
	{
		return null;
	}

	public static <LBL> IFSALabelCondition<LBL> createEq(LBL label)
	{
		return new AbstractLabelCondition<LBL>(label)
		{
			@Override
			public boolean test(LBL element)
			{
				return Objects.equals(label, element);
			}

			@Override
			public Collection<LBL> getLabels()
			{
				return Collections.singleton(label);
			}
		};
	}

	public static <LBL> IFSALabelCondition<LBL> createAnyOrEq(LBL label)
	{
		if (label == null)
			return createAny();

		return createEq(label);
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

	public static boolean isAny(IFSALabelCondition<?> labelCondition)
	{
		return labelCondition == any;
	}

	public static boolean isAnyLoop(IFSALabelCondition<?> labelCondition)
	{
		return labelCondition == anyLoop;
	}
}
