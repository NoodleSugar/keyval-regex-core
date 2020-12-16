package insomnia.unifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import insomnia.data.IPath;
import insomnia.data.PathOp;
import insomnia.rule.IPathRule;

public final class PathUnifiers<V, E>
{
	public static <V, E> int[] findAllPrefixSuffix(IPath<V, E> p, IPath<V, E> s)
	{
		return PathOp.findProperSimplePrefixSuffix(p, s);
	}

	// Inclusions strictes (limites exclues)
	public static <V, E> int[] findAllInclusions(IPath<V, E> i, IPath<V, E> c)
	{
		return PathOp.findAllInclusions(i, c);
	}

	// =========================================================================

	private IPathUnifierFactory<V, E, IPathUnifier<V, E>> unifierFactory;

	public PathUnifiers(IPathUnifierFactory<V, E, IPathUnifier<V, E>> ufactory)
	{
		unifierFactory = ufactory;
	}

	public PathUnifiers(Class<IPathUnifier<V, E>> unifierClass) throws NoSuchMethodException, SecurityException
	{
		Constructor<IPathUnifier<V, E>> constructor = unifierClass.getDeclaredConstructor(IPath.class, IPath.class, IPath.class, IPath.class, IPath.class);

		/*
		 * Create the factory with the unifier's class constructor
		 */
		unifierFactory = new AbstractPathUnifierFactory<V, E, IPathUnifier<V, E>>()
		{
			@Override
			public IPathUnifier<V, E> get(IPath<V, E> pb, IPath<V, E> sb, IPath<V, E> ph, IPath<V, E> sh, IPath<V, E> ref)
			{
				try
				{
					return (IPathUnifier<V, E>) constructor.newInstance(pb, sb, ph, sh, ref);
				}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
	}

	public Collection<IPathUnifier<V, E>> weakUnifiers(IPath<V, E> head, IPath<V, E> body, boolean existentialHead)
	{
		return weakUnifiers(head, body, false, existentialHead);
	}

	public Collection<IPathUnifier<V, E>> weakUnifiers(IPath<V, E> head, IPath<V, E> body)
	{
		return weakUnifiers(head, body, false, false);
	}

	public Collection<IPathUnifier<V, E>> strongUnifiers(IPath<V, E> head, IPath<V, E> body)
	{
		return strongUnifiers(head, body, false);
	}

	public Collection<IPathUnifier<V, E>> weakUnifiers(IPathRule<V, E> a, IPathRule<V, E> b)
	{
		return weakUnifiers(a.getHead(), b.getBody(), false, a.isExistential());
	}

	public Collection<IPathUnifier<V, E>> strongUnifiers(IPathRule<V, E> a, IPathRule<V, E> b)
	{
		return strongUnifiers(a.getHead(), b.getBody(), false);
	}

	public Collection<IPathUnifier<V, E>> weakUnifiers(IPathRule<V, E> a, IPathRule<V, E> b, boolean firstFind)
	{
		return weakUnifiers(a.getHead(), b.getBody(), firstFind, a.isExistential());
	}

	public Collection<IPathUnifier<V, E>> strongUnifiers(IPathRule<V, E> a, IPathRule<V, E> b, boolean firstFind)
	{
		return strongUnifiers(a.getHead(), b.getBody(), firstFind);
	}

	public Collection<IPathUnifier<V, E>> weakUnifiers(IPath<V, E> head, IPath<V, E> body, boolean firstFind, boolean existentialHead)
	{
		List<IPathUnifier<V, E>> ret = new ArrayList<>();

		final int h_size = head.size();
		final int b_size = body.size();

		int tmp[];
		int i;

		/*
		 * head : y.A
		 * body : (∃)? B.y
		 */
		tmp = PathOp.findOverlappedPossiblePrefixes(body, head, firstFind);

		for (i = 0; i < tmp.length; i++)
		{
			int size = tmp[i];
			ret.add(unifierFactory.get( //
				body.subPath(0, b_size - size), null, //
				null, head.subPath(size, h_size), //
				head.subPath(0, size)));
		}
		/*
		 * head : y
		 * body : (∃)? B.y
		 */
		if (PathOp.isProperSuffix(head, body))
		{
			ret.add(unifierFactory.get( //
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
			tmp = PathOp.findOverlappedPossiblePrefixes(head, body, firstFind);

			for (i = 0; i < tmp.length; i++)
			{
				int size = tmp[i];
				ret.add(unifierFactory.get( //
					null, body.subPath(size, b_size), //
					head.subPath(0, h_size - size), null, //
					body.subPath(0, size)));
			}
			/*
			 * head : y
			 * body : y.B
			 */
			if (PathOp.isProperPrefix(head, body))
			{
				ret.add(unifierFactory.get( //
					null, body.subPath(h_size, b_size), //
					null, null, //
					head));
			}
			/*
			 * head : y
			 * body : B1.y.B2
			 */
			tmp = PathOp.findInclusions(head, body, firstFind, true);

			for (i = 0; i < tmp.length; i++)
			{
				int pos = tmp[i];

				ret.add(unifierFactory.get( //
					body.subPath(0, pos), body.subPath(pos + h_size, b_size), //
					null, null, //
					head));
			}
		}

		if (firstFind && ret.size() > 1)
			return ret.subList(0, 1);

		return ret;
	}

	public Collection<IPathUnifier<V, E>> strongUnifiers(IPath<V, E> head, IPath<V, E> body, boolean firstFind)
	{
		List<IPathUnifier<V, E>> ret = new ArrayList<>();

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
		if (PathOp.isSuffix(body, head, false))
		{
			ret.add(unifierFactory.get( //
				null, null, //
				head.subPath(0, h_size - b_size), null, //
				body));
		}
		/*
		 * head : y.A
		 * body : y
		 */
		if (PathOp.isPrefix(body, head, true))
		{
			ret.add(unifierFactory.get( //
				null, null, //
				null, head.subPath(b_size, h_size), //
				body));
		}
		/*
		 * head : A1.y.A2
		 * body : y
		 */
		{
			tmp = PathOp.findInclusions(body, head, firstFind, true);

			for (i = 0; i < tmp.length; i++)
			{
				int pos = tmp[i];

				ret.add(unifierFactory.get( //
					null, null, //
					head.subPath(0, pos), head.subPath(pos + b_size, h_size), //
					body));
			}
		}

		if (firstFind && ret.size() > 1)
			return ret.subList(0, 1);

		return ret;
	}

	public boolean haveUnifiers(IPathRule<V, E> a, IPathRule<V, E> b)
	{
		if (!strongUnifiers(a, b, true).isEmpty())
			return true;

		if (!weakUnifiers(a, b, true).isEmpty())
			return true;

		return false;
	}

	public Collection<IPathUnifier<V, E>> compute(IPathRule<V, E> a, IPathRule<V, E> b)
	{
		List<IPathUnifier<V, E>> ret = new ArrayList<>();
		ret.addAll(weakUnifiers(a, b));
		ret.addAll(strongUnifiers(a, b));
		return ret;
	}

	public Collection<IPathUnifier<V, E>> compute(IPath<V, E> head, IPath<V, E> body)
	{
		return compute(head, body, false);
	}

	public Collection<IPathUnifier<V, E>> compute(IPath<V, E> head, IPath<V, E> body, boolean existentialHead)
	{
		List<IPathUnifier<V, E>> ret = new ArrayList<>();
		ret.addAll(weakUnifiers(head, body, existentialHead));
		ret.addAll(strongUnifiers(head, body, existentialHead));
		return ret;
	}
}
