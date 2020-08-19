package suffixsystem.unifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;

import insomnia.rule.tree.Path;
import insomnia.suffixsystem.unifier.Unifier;

class TestUnifier
{
	private final static String separator = Pattern.quote(".");

	@ParameterizedTest
	@CsvSource({ //
			"a      , a      , 0, 0", //
			"a.b    , b.a.b.a, 1, 1, 1, 1", //
			"a.b.a  , b.a.b.a, 1, 1, 1, 2", //
			"a.a.a.b, a.a.a.a, 3, 0, 1, 2, 3", //
			"a.b.a.b, b.a.b.a, 2, 2, 1, 3, 1, 3" //

	})
	void findPrefixSuffix(ArgumentsAccessor args)
	{
		Path body = new Path(args.getString(0).split(separator));
		Path head = new Path(args.getString(1).split(separator));

		int n1 = args.getInteger(2);
		int n2 = args.getInteger(3);

		int[] b_in_h = Unifier.findAllPrefixSuffix(body, head);
		assertEquals(n1, b_in_h.length);
		for (int i = 0; i < n1; i++)
			assertEquals((int) args.getInteger(4 + i), b_in_h[i]);

		int[] h_in_b = Unifier.findAllPrefixSuffix(head, body);
		assertEquals(n2, h_in_b.length);
		for (int i = 0; i < n2; i++)
			assertEquals((int) args.getInteger(4 + n1 + i), h_in_b[i]);
	}

	@ParameterizedTest
	@CsvSource({ //
			"a.b  , b.a.b.a.b.a, 2, 1, 3", //
			"a.b.c, x.a.b.e.c.z, 0", //
			"a.b.c, e.a.b.c.f  , 1, 1" //
	})
	void findInclusions(ArgumentsAccessor args)
	{
		Path body = new Path(args.getString(0).split(separator));
		Path head = new Path(args.getString(1).split(separator));

		int n = args.getInteger(2);

		int[] inc = Unifier.findAllInclusions(body, head);
		assertEquals(n, inc.length);

		for (int i = 0; i < n; i++)
			assertEquals((int) args.getInteger(3 + i), inc[i]);
	}

	@ParameterizedTest
	// b, h, number, [pb, sb, ph, sh, ...]
	@CsvSource({ //
			"a.b, b.a.b.a, 3," + " a, , , a.b.a," + " , b, b.a.b, ," + " , , b, a", //
			"a.a, a.a.a  , 4," + " a, , , a.a," + " , , , a," + " , , a, ," + " , a, a.a, " //
	})
	void compute(ArgumentsAccessor args)
	{
		Path body = new Path(args.getString(0).split(separator));
		Path head = new Path(args.getString(1).split(separator));
		int  n    = args.getInteger(2).intValue();

		List<Unifier> unifiers = Unifier.compute(body, head);
		assertEquals(n, unifiers.size());

		ArrayList<String[]> a = new ArrayList<>();
		for (int i = 0; i < n; i++)
		{
			String pb = args.getString(3 + 4 * i + 0);
			String sb = args.getString(3 + 4 * i + 1);
			String ph = args.getString(3 + 4 * i + 2);
			String sh = args.getString(3 + 4 * i + 3);

			a.add(new String[] { pb, sb, ph, sh });
		}

		for (Unifier u : unifiers)
			assertTrue(contains(a, u));
	}

	private boolean contains(List<String[]> a, Unifier u)
	{
		String pb, sb, ph, sh;
		if (u.prefixBody == null)
			pb = null;
		else
			pb = StringUtils.join(u.prefixBody.getLabels(), '.');
		if (u.suffixBody == null)
			sb = null;
		else
			sb = StringUtils.join(u.suffixBody.getLabels(), '.');
		if (u.prefixHead == null)
			ph = null;
		else
			ph = StringUtils.join(u.prefixHead.getLabels(), '.');
		if (u.suffixHead == null)
			sh = null;
		else
			sh = StringUtils.join(u.suffixHead.getLabels(), '.');

		String[] unif = { pb, sb, ph, sh };

		loop: for (String[] line : a)
		{
			for (int i = 0; i < 4; i++)
			{
				if (!(line[i] == null && unif[i] == null //
					|| line[i] != null && line[i].equals(unif[i])))
					continue loop;
			}
			return true;
		}
		return false;
	}
}
