package insomnia.suffixsystem.unifier;

import java.util.ArrayList;
import java.util.List;

import insomnia.rule.tree.Path;

public class Unifier
{
	public Path prefixBody;
	public Path suffixBody;
	public Path prefixHead;
	public Path suffixHead;

	private Unifier(Path pb, Path sb, Path ph, Path sh)
	{
		prefixBody = pb;
		suffixBody = sb;
		prefixHead = ph;
		suffixHead = sh;
	}

	// Préfixes et suffixes stricts (limites exclues)
	public static int[] findAllPrefixSuffix(Path p, Path s)
	{
		ArrayList<Integer> array = new ArrayList<>();

		List<String> keys1 = p.getLabels();
		List<String> keys2 = s.getLabels();

		int n1  = keys1.size();
		int n2  = keys2.size();
		int min = Math.min(n1, n2);

		loop_ps: for (int len = 1; len < min; len++)
		{
			for (int offset = 0; offset < len; offset++)
			{
				if (!keys1.get(offset).equals(keys2.get(n2 - len + offset)))
					continue loop_ps;
			}
			array.add(len);
		}
		return array.stream().mapToInt(Integer::intValue).toArray();
	}

	// Inclusions strictes (limites exclues)
	public static int[] findAllInclusions(Path i, Path c)
	{
		ArrayList<Integer> array = new ArrayList<>();

		List<String> keys1 = i.getLabels();
		List<String> keys2 = c.getLabels();

		int n1 = keys1.size();
		int n2 = keys2.size();

		loop_inc: for (int shift = 1; shift < n2 - n1; shift++)
		{
			for (int offset = 0; offset < n1; offset++)
			{
				if (!keys1.get(offset).equals(keys2.get(shift + offset)))
					continue loop_inc;
			}
			array.add(shift);
		}
		return array.stream().mapToInt(Integer::intValue).toArray();
	}

	// rule1 dépend de rule2
	public static List<Unifier> compute(Path body, Path head)
	{
		List<Unifier> unifiers = new ArrayList<>();

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
			if (!body.isRooted() && body.isSuffix(head))
				unifiers.add(new Unifier(null, null, new Path(head, 0, n2 - n1), null));

			// Si body n'est pas terminal
			// Si body est un préfixe de head
			if (!body.isTerminal() && body.isPrefix(head))
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
			if (!head.isRooted() && head.isSuffix(body))
				unifiers.add(new Unifier(new Path(body, 0, n1 - n2), null, null, null));

			// Si head n'est pas terminal
			// Si head est un préfixe de body
			if (head.isTerminal() && head.isPrefix(body))
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

		return unifiers;
	}
}
