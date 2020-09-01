package insomnia.suffixsystem.unifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import insomnia.rule.tree.Path;
import insomnia.rule.tree.Paths;

public class Unifier
{
	public Path prefixBody;
	public Path suffixBody;
	public Path prefixHead;
	public Path suffixHead;
	public Path reference;

	static private Path emptyPath = new Path();

	public Unifier(Path pb, Path sb, Path ph, Path sh)
	{
		this(pb, sb, ph, sh, null);
	}

	public Unifier(Path pb, Path sb, Path ph, Path sh, Path ref)
	{
		prefixBody = ObjectUtils.defaultIfNull(pb, emptyPath);
		suffixBody = ObjectUtils.defaultIfNull(sb, emptyPath);
		prefixHead = ObjectUtils.defaultIfNull(ph, emptyPath);
		suffixHead = ObjectUtils.defaultIfNull(sh, emptyPath);
		reference  = ObjectUtils.defaultIfNull(ref, emptyPath);
	}

	public boolean emptyBody()
	{
		return prefixBody.isEmpty() && suffixBody.isEmpty();
	}

	public boolean emptyHead()
	{
		return prefixHead.isEmpty() && suffixHead.isEmpty();
	}

	// Préfixes et suffixes stricts (limites exclues)
	public static int[] findAllPrefixSuffix(Path p, Path s)
	{
		return Paths.findProperSimplePrefixSuffix(p, s);
	}

	// Inclusions strictes (limites exclues)
	public static int[] findAllInclusions(Path i, Path c)
	{
		return Paths.findAllInclusions(i, c);
	}

	public static List<Unifier> weakUnifiers(Path head, Path body, boolean existentialHead)
	{
		return weakUnifiers(head, body, false, existentialHead);
	}

	public static List<Unifier> weakUnifiers(Path head, Path body)
	{
		return weakUnifiers(head, body, false, false);
	}

	public static List<Unifier> strongUnifiers(Path head, Path body)
	{
		return strongUnifiers(head, body, false);
	}

	public static List<Unifier> weakUnifiers(Path head, Path body, boolean firstFind, boolean existentialHead)
	{
		List<Unifier> ret = new ArrayList<>();

		final int h_size = head.size();
		final int b_size = body.size();

		int tmp[];
		int i;

		/*
		 * head : y.A
		 * body : B.y
		 */
		tmp = Paths.findOverlappedPossiblePrefixes(body, head, firstFind);

		for (i = 0; i < tmp.length; i++)
		{
			int size = tmp[i];
			ret.add(new Unifier( //
				body.subPath(0, b_size - size), null, //
				null, head.subPath(size, h_size), //
				head.subPath(0, size)));
		}
		/*
		 * head : y
		 * body : B.y
		 */
		if (Paths.isProperSuffix(head, body))
		{
			ret.add(new Unifier( //
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
			tmp = Paths.findOverlappedPossiblePrefixes(head, body, firstFind);

			for (i = 0; i < tmp.length; i++)
			{
				int size = tmp[i];
				ret.add(new Unifier( //
					null, body.subPath(size, b_size), //
					head.subPath(0, h_size - size), null, //
					body.subPath(0, size)));
			}
			/*
			 * head : y
			 * body : y.B
			 */
			if (Paths.isProperPrefix(head, body))
			{
				ret.add(new Unifier( //
					null, body.subPath(h_size, b_size), //
					null, null, //
					head));
			}
			/*
			 * head : y
			 * body : B1.y.B2
			 */
			tmp = Paths.findInclusions(head, body, firstFind, true);

			for (i = 0; i < tmp.length; i++)
			{
				int pos = tmp[i];

				ret.add(new Unifier( //
					body.subPath(0, pos), body.subPath(pos + h_size, b_size), //
					null, null, //
					head));
			}
		}

		if (firstFind && ret.size() > 1)
			return ret.subList(0, 1);

		return ret;
	}

	public static List<Unifier> strongUnifiers(Path head, Path body, boolean firstFind)
	{
		List<Unifier> ret = new ArrayList<>();

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
		if (Paths.isSuffix(body, head, false))
		{
			ret.add(new Unifier( //
				null, null, //
				head.subPath(0, h_size - b_size), null, //
				body));
		}
		/*
		 * head : y.A
		 * body : y
		 */
		if (Paths.isPrefix(body, head, true))
		{
			ret.add(new Unifier( //
				null, null, //
				null, head.subPath(b_size, h_size), //
				body));
		}
		/*
		 * head : A1.y.A2
		 * body : y
		 */
		{
			tmp = Paths.findInclusions(body, head, firstFind, true);

			for (i = 0; i < tmp.length; i++)
			{
				int pos = tmp[i];

				ret.add(new Unifier( //
					null, null, //
					head.subPath(0, pos), head.subPath(pos + b_size, h_size), //
					body));
			}
		}

		if (firstFind && ret.size() > 1)
			return ret.subList(0, 1);

		return ret;
	}

	// rule1 dépend de rule2
	public static List<Unifier> compute(Path head, Path body)
	{
		List<Unifier> ret = weakUnifiers(head, body);
		ret.addAll(strongUnifiers(head, body));
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;

		if (!(obj instanceof Unifier))
			return false;

		Unifier u = (Unifier) obj;
		return this.prefixBody.equals(u.prefixBody) //
			&& this.suffixBody.equals(u.suffixBody) //
			&& this.prefixHead.equals(u.prefixHead) //
			&& this.suffixHead.equals(u.suffixHead) //
			&& this.reference.equals(u.reference);
	}

	@Override
	public int hashCode()
	{
		return this.prefixBody.hashCode() //
			+ this.suffixBody.hashCode() //
			+ this.prefixHead.hashCode() //
			+ this.suffixHead.hashCode() //
			+ this.reference.hashCode();
	}

	@Override
	public String toString()
	{
		return "B:" + prefixBody + "_" + suffixBody //
			+ " H:" + prefixHead + "_" + suffixHead //
			+ " _:" + reference;
	}
}
