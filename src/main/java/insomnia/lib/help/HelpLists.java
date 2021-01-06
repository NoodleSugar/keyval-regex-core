package insomnia.lib.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import insomnia.lib.numeric.Base;

public final class HelpLists
{
	public static <A, B> List<Object[]> mergePairsArrays(Collection<Pair<A[], B[]>> pairs)
	{
		if (pairs.size() == 0)
			return java.util.Collections.emptyList();

		List<Object[]> ret = new ArrayList<>(pairs.size());

		for (Pair<A[], B[]> pair : pairs)
			ret.add(ArrayUtils.addAll(pair.getLeft(), pair.getRight()));

		return ret;
	}

	public static List<List<Object>> mergePairsContent(Collection<Pair<? extends Collection<?>, ? extends Collection<?>>> pairs)
	{
		if (pairs.size() == 0)
			return java.util.Collections.emptyList();

		List<List<Object>> ret = new ArrayList<>(pairs.size());

		for (Pair<? extends Collection<?>, ? extends Collection<?>> pair : pairs)
			ret.add(mergePairContent(pair));

		return ret;
	}

	private static List<Object> mergePairContent(Pair<? extends Collection<?>, ? extends Collection<?>> pair)
	{
		List<Object> ret = new ArrayList<Object>();
		ret.addAll(pair.getLeft());
		ret.addAll(pair.getRight());
		return ret;
	}

	public static <A, B> List<Pair<A, B>> product(List<A> a, List<B> b)
	{
		int  nb    = a.size() * b.size();
		Base base  = new Base(a.size(), b.size());
		int  num[] = new int[2];
		Arrays.fill(num, 0);

		List<Pair<A, B>> ret = new ArrayList<>(nb);

		for (int i = 0; i < nb; i++)
		{
			ret.add(Pair.of(a.get(num[0]), b.get(num[1])));
			base.increment(num);
		}
		return ret;
	}
}
