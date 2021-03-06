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
		Object obj;

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

	// =========================================================================

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

	// =========================================================================

	private static class EqCondition<LBL> extends AbstractLabelCondition<LBL>
	{
		EqCondition(LBL label)
		{
			super(label);
		}

		@Override
		public boolean test(LBL element)
		{
			return Objects.equals(obj, element);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Collection<LBL> getLabels()
		{
			return Collections.singleton((LBL) obj);
		}
	}

	public static <LBL> IFSALabelCondition<LBL> createEq(LBL label)
	{
		return new EqCondition<>(label);
	}

	public static <LBL> IFSALabelCondition<LBL> createAnyOrEq(LBL label)
	{
		if (label == null)
			return createAny();

		return createEq(label);
	}

	// =========================================================================

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

	// =========================================================================

	public static boolean isAny(IFSALabelCondition<?> labelCondition)
	{
		return labelCondition == any || labelCondition == anyLoop;
	}

	public static boolean isAnyLoop(IFSALabelCondition<?> labelCondition)
	{
		return labelCondition == anyLoop;
	}


	// =========================================================================

	public static <LBL> boolean projectOnMe(IFSALabelCondition<LBL> a, IFSALabelCondition<LBL> b)
	{
		return projectOn(b, a);
	}

	public static <LBL> boolean projectOn(IFSALabelCondition<LBL> a, IFSALabelCondition<LBL> b)
	{
		if (a == b)
			return true;
		if (isAny(a))
			return true;

		if (a instanceof EqCondition<?> && b instanceof EqCondition<?> && Objects.equals(((EqCondition<?>) a).obj, ((EqCondition<?>) b).obj))
			return true;

		return false;
	}

	// =========================================================================

	public static <VAL, LBL> IFSALabelCondition<VAL> intersection(IFSALabelCondition<VAL> a, IFSALabelCondition<VAL> b)
	{
		if (isAny(a))
			return b;
		if (isAny(b))
			return a;

		if (a instanceof EqCondition<?> && b instanceof EqCondition<?> && Objects.equals(((EqCondition<?>) a).obj, ((EqCondition<?>) b).obj))
			return a;

		throw new IllegalArgumentException();
	}
}
