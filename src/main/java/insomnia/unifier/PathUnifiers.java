package insomnia.unifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import insomnia.data.IPath;
import insomnia.rule.IPathRule;

/**
 * Factory of {@link IPathUnifier} able to compute {@link IPathUnifier}s (strong/weak/all) depending on initials {@link IPath} or {@link IPathRule} .
 * 
 * @author zuri
 */
public final class PathUnifiers<VAL, LBL>
{
	private IPathUnifierFactory<VAL, LBL, IPathUnifier<VAL, LBL>> unifierFactory;

	/**
	 * Use internally a {@link IPathUnifierFactory} to create the concrete {@link IPathUnifier} objects when needed.
	 */
	public PathUnifiers(IPathUnifierFactory<VAL, LBL, IPathUnifier<VAL, LBL>> ufactory)
	{
		unifierFactory = ufactory;
	}

	/**
	 * Use 'unifierClass' as a concrete class of {@link IPathUnifier}.
	 * This constructor will search a constructor in 'unifierClass' to create objects of type {@link IPathUnifier} when needed.
	 * 
	 * @param unifierClass A class of type {@link IPathUnifier} that must have a constructor signature like {@link IPathUnifierFactory#create(IPath, IPath, IPath, IPath, IPath)}.
	 * @throws NoSuchMethodException If the constructor does not exists
	 * @throws SecurityException     If the constructor is not accessible (see {@link Class#getDeclaredConstructor(Class...) })
	 * @throws RuntimeException      If cannot instantiate a {@link IPathUnifier} when constructor is called later.
	 */
	public PathUnifiers(Class<? extends IPathUnifier<VAL, LBL>> unifierClass) throws NoSuchMethodException, SecurityException
	{
		Constructor<? extends IPathUnifier<VAL, LBL>> constructor = unifierClass.getDeclaredConstructor(IPath.class, IPath.class, IPath.class, IPath.class, IPath.class);

		/*
		 * Create the factory with the unifier's class constructor
		 */
		unifierFactory = new AbstractPathUnifierFactory<VAL, LBL, IPathUnifier<VAL, LBL>>()
		{
			@Override
			public IPathUnifier<VAL, LBL> create(IPath<VAL, LBL> pb, IPath<VAL, LBL> sb, IPath<VAL, LBL> ph, IPath<VAL, LBL> sh, IPath<VAL, LBL> ref)
			{
				try
				{
					return (IPathUnifier<VAL, LBL>) constructor.newInstance(pb, sb, ph, sh, ref);
				}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
	}

	public Collection<IPathUnifier<VAL, LBL>> weakUnifiers(IPath<VAL, LBL> head, IPath<VAL, LBL> body, boolean existentialHead)
	{
		return weakUnifiers(head, body, false, existentialHead);
	}

	public Collection<IPathUnifier<VAL, LBL>> weakUnifiers(IPath<VAL, LBL> head, IPath<VAL, LBL> body)
	{
		return weakUnifiers(head, body, false, false);
	}

	public Collection<IPathUnifier<VAL, LBL>> strongUnifiers(IPath<VAL, LBL> head, IPath<VAL, LBL> body)
	{
		return strongUnifiers(head, body, false);
	}

	public Collection<IPathUnifier<VAL, LBL>> weakUnifiers(IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b)
	{
		return weakUnifiers(a.getHead(), b.getBody(), false, a.isExistential());
	}

	public Collection<IPathUnifier<VAL, LBL>> strongUnifiers(IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b)
	{
		return strongUnifiers(a.getHead(), b.getBody(), false);
	}

	public Collection<IPathUnifier<VAL, LBL>> weakUnifiers(IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b, boolean firstFind)
	{
		return weakUnifiers(a.getHead(), b.getBody(), firstFind, a.isExistential());
	}

	public Collection<IPathUnifier<VAL, LBL>> strongUnifiers(IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b, boolean firstFind)
	{
		return strongUnifiers(a.getHead(), b.getBody(), firstFind);
	}

	/**
	 * Find all the weak unifiers from head to body according to the path rewriting framework.
	 * The weak unifiers are these where the body's suffix or prefix is not empty.
	 * 
	 * @param head            Head, may be from a rule
	 * @param body            Body, may be from a rule
	 * @param firstFind       Stop the process at the first one founded
	 * @param existentialHead If head is an existential one
	 * @return All the strong unifiers.
	 */
	private Collection<IPathUnifier<VAL, LBL>> weakUnifiers(IPath<VAL, LBL> head, IPath<VAL, LBL> body, boolean firstFind, boolean existentialHead)
	{
		List<IPathUnifier<VAL, LBL>> ret = new ArrayList<>();

		final int h_size = head.size();
		final int b_size = body.size();

		int tmp[];
		int i;

		/*
		 * head : y.A
		 * body : (∃)? B.y
		 */
		tmp = IPath.findOverlappedPossiblePrefixes(body, head, firstFind);

		for (i = 0; i < tmp.length; i++)
		{
			int size = tmp[i];
			ret.add(unifierFactory.create( //
				body.subPath(0, b_size - size), null, //
				null, head.subPath(size, h_size), //
				head.subPath(0, size)));
		}
		/*
		 * head : y
		 * body : (∃)? B.y
		 */
		if (IPath.isProperSuffix(head, body))
		{
			ret.add(unifierFactory.create( //
				body.subPath(0, b_size - h_size), null, //
				null, null, //
				head));
		}

		if (!existentialHead)
		{
			/*
			 * head : A.y
			 * body : y.B
			 */
			tmp = IPath.findOverlappedPossiblePrefixes(head, body, firstFind);

			for (i = 0; i < tmp.length; i++)
			{
				int size = tmp[i];
				ret.add(unifierFactory.create( //
					null, body.subPath(size, b_size), //
					head.subPath(0, h_size - size), null, //
					body.subPath(0, size)));
			}
			/*
			 * head : y
			 * body : y.B
			 */
			if (IPath.isProperPrefix(head, body))
			{
				ret.add(unifierFactory.create( //
					null, body.subPath(h_size, b_size), //
					null, null, //
					head));
			}
			/*
			 * head : y
			 * body : B1.y.B2
			 */
			tmp = IPath.findInclusions(head, body, firstFind, true);

			for (i = 0; i < tmp.length; i++)
			{
				int pos = tmp[i];

				ret.add(unifierFactory.create( //
					body.subPath(0, pos), body.subPath(pos + h_size, b_size), //
					null, null, //
					head));
			}
		}

		if (firstFind && ret.size() > 1)
			return ret.subList(0, 1);

		return ret;
	}

	/**
	 * Find all the strong unifiers from head to body according to the path rewriting framework.
	 * The strong unifiers are these where the body's suffix and prefix are empty.
	 * <p>
	 * Note:
	 * The computation for an existential head is the same as for the non existential one, so there is no parameter 'existentialHead' like in {@link #weakUnifiers(IPath, IPath, boolean, boolean)}.
	 * </p>
	 * 
	 * @param head      Head, may be from a rule
	 * @param body      Body, may be from a rule
	 * @param firstFind Stop the process at the first one founded
	 * @return All the strong unifiers.
	 */
	private Collection<IPathUnifier<VAL, LBL>> strongUnifiers(IPath<VAL, LBL> head, IPath<VAL, LBL> body, boolean firstFind)
	{
		List<IPathUnifier<VAL, LBL>> ret = new ArrayList<>();

		final int h_size = head.size();
		final int b_size = body.size();

		int tmp[];
		int i;

		/*
		 * head : y
		 * body : y
		 * OR
		 * head : A.y
		 * body : y
		 */
		if (IPath.isSuffix(body, head, false))
		{
			ret.add(unifierFactory.create( //
				null, null, //
				head.subPath(0, h_size - b_size), null, //
				body));
		}
		/*
		 * head : y.A
		 * body : y
		 */
		if (IPath.isPrefix(body, head, true))
		{
			ret.add(unifierFactory.create( //
				null, null, //
				null, head.subPath(b_size, h_size), //
				body));
		}
		/*
		 * head : A1.y.A2
		 * body : y
		 */
		{
			tmp = IPath.findInclusions(body, head, firstFind, true);

			for (i = 0; i < tmp.length; i++)
			{
				int pos = tmp[i];

				ret.add(unifierFactory.create( //
					null, null, //
					head.subPath(0, pos), head.subPath(pos + b_size, h_size), //
					body));
			}
		}

		if (firstFind && ret.size() > 1)
			return ret.subList(0, 1);

		return ret;
	}

	public boolean haveUnifiers(IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b)
	{
		if (!strongUnifiers(a, b, true).isEmpty())
			return true;

		if (!weakUnifiers(a, b, true).isEmpty())
			return true;

		return false;
	}

	public Collection<IPathUnifier<VAL, LBL>> compute(IPathRule<VAL, LBL> a, IPathRule<VAL, LBL> b)
	{
		List<IPathUnifier<VAL, LBL>> ret = new ArrayList<>();
		ret.addAll(weakUnifiers(a, b));
		ret.addAll(strongUnifiers(a, b));
		return ret;
	}

	public Collection<IPathUnifier<VAL, LBL>> compute(IPath<VAL, LBL> head, IPath<VAL, LBL> body)
	{
		return compute(head, body, false);
	}

	public Collection<IPathUnifier<VAL, LBL>> compute(IPath<VAL, LBL> head, IPath<VAL, LBL> body, boolean existentialHead)
	{
		List<IPathUnifier<VAL, LBL>> ret = new ArrayList<>();
		ret.addAll(weakUnifiers(head, body, existentialHead));
		ret.addAll(strongUnifiers(head, body, existentialHead));
		return ret;
	}
}
