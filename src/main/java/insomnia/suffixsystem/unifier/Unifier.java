package insomnia.suffixsystem.unifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	// rule1 dépend de rule2
	public static List<Unifier> compute(Path body, Path head)
	{
		Set<Unifier> unifiers = new HashSet<>();

		int n1 = body.size();
		int n2 = head.size();

		// Si body et head ont la même taille
		if (n1 == n2)
		{
			// Si body et head sont égaux
			if (body.equals(head))
				unifiers.add(new Unifier(null, null, null, null, body));
		}
		// Si body est plus petit que head
		else if (n1 < n2)
		{
			// Si body n'est pas enraciné
			// Si body est un suffixe de head
			if (!body.isRooted() && Paths.isProperSimpleSuffix(body, head))
				unifiers.add(new Unifier(null, null, new Path(head, 0, n2 - n1), null));

			// Si body n'est pas terminal
			// Si body est un préfixe de head
			if (!body.isTerminal() && Paths.isProperSimplePrefix(body, head))
				unifiers.add(new Unifier(null, null, null, new Path(head, n1, n2)));

			// Si body n'est pas enraciné
			// Si body n'est pas terminal
			if (!body.isRooted() && !body.isTerminal())
			{
				// Inclusions strictes de body dans head
				int[] indexs = findAllInclusions(body, head);
				for (int i : indexs)
				{
					Path ph, sh;
					if (i == 0)
						ph = null;
					else
						ph = new Path(head, 0, i);

					if (i + n1 == n2)
						sh = null;
					else
						sh = new Path(head, i + n1, n2);

					unifiers.add(new Unifier(null, null, ph, sh));
				}

			}
		}
		// Si body est plus grand que head
		else
		{
			// Si head n'est pas enraciné
			// Si head est un suffixe de body
			if (!head.isRooted() && Paths.isProperSimpleSuffix(head, body))
				unifiers.add(new Unifier(new Path(body, 0, n1 - n2), null, null, null));

			// Si head n'est pas terminal
			// Si head est un préfixe de body
			if (head.isTerminal() && Paths.isProperSimplePrefix(head, body))
				unifiers.add(new Unifier(null, new Path(body, n2, n1), null, null));

			// Si head n'est pas enraciné
			// Si head n'est pas terminal
			if (!head.isRooted() && !head.isTerminal())
			{
				// Inclusions strictes de head dans body
				int[] indexs = findAllInclusions(head, body);
				for (int i : indexs)
				{
					Path pb, sb;
					if (i == 0)
						pb = null;
					else
						pb = new Path(body, 0, i);

					if (i + n2 == n1)
						sb = null;
					else
						sb = new Path(body, i + n2, n1);

					unifiers.add(new Unifier(pb, sb, null, null));
				}
			}
		}

		// Si body n'est pas enraciné
		// Si head n'est pas terminal
		if (!body.isRooted() && !head.isTerminal())
		{
			// Calcul des préfixes de body qui sont suffixes de head
			int[] lengths = findAllPrefixSuffix(body, head);
			for (int i : lengths)
				unifiers.add(new Unifier(null, new Path(body, i, n1), new Path(head, 0, n2 - i), null));
		}

		// Si head n'est pas enraciné
		// Si body n'est pas terminal
		if (!head.isRooted() && !body.isTerminal())
		{
			// Calcul des préfixes de head qui sont suffixes de body
			int[] lengths = findAllPrefixSuffix(head, body);
			for (int i : lengths)
				unifiers.add(new Unifier(new Path(body, 0, n1 - i), null, null, new Path(head, i, n2)));
		}

		return new ArrayList<>(unifiers);
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
		return "B=" + prefixBody + "___" + suffixBody //
			+ " H=" + prefixHead + "___" + suffixHead;
	}
}
