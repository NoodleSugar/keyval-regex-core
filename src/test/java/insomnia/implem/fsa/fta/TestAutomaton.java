package insomnia.implem.fsa.fta;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fta.IBUFTA;
import insomnia.implem.data.Trees;
import insomnia.implem.data.regex.parser.PRegexParser;
import insomnia.implem.fsa.fta.creational.BUBuilder;
import insomnia.lib.help.HelpLists;

public class TestAutomaton
{
	// ==========================================================================

	static List<Object[]> mergeParameters(List<Object[]> a, List<Object[]> b)
	{
		List<Pair<Object[], Object[]>> ret    = HelpLists.product(a, b);
		List<Object[]>                 merged = HelpLists.mergePairsArrays(ret);
		return merged;
	}

	static List<Object[]> match() throws ParseException
	{
		/*
		 * searchFor -> { searchIn }
		 */
		List<Object[][]> a = //
			Arrays.asList(ArrayUtils.addAll(new Object[][][] { //
					{ { "a.c" }, //
							{ "(^)?.X?.a(b,c.X?)", true }, //
							{ "(^)?.X?.a(b(c.X?))", false }, //
					}, //
					{ { "a(b,c)" }, //
							{ "(^)?.X?.a.(=1)?.(b.(=1)?.(X)?.($)?,c.(=1)?.X?.($)?)", true }, //
							{ "a(b|c,X)", false }, //
					}, //
					{ { "^a(b,c)" }, //
							{ "^a(b,c)", true }, //
							{ "((^)?.X)?.a(b,c)", false }, //
					}, //
					{ { "a(b,c$)" }, //
							{ "(^)?.X?.a(b.X?.($)?,c.(=1)?.$)", true }, //
							{ "a(b,c)", false }, //
							{ "a(X,c$)", false }, //
					}, //
					{ { "^a(aa,ab=5$),b$" }, //
							{ "^a(aa.(=0)?.X?.($)?,ab=5$),b$", true }, //
							{ "^a(aa.X?,ab$),b$", false }, //
							{ "^a(aa.X?,ab=6$),b$", false }, //
							{ ".a(aa.X?,ab=5$),b$", false }, //
							{ "^a(aa.X?,ab=5$),b.X?", false }, //
					}, //
					{ { "^=1" }, //
							{ "^=1", true }, //
							{ "X{0,2}.=1", false }, //
					}, //
					{ { "a=1" }, //
							{ "(^)?.X?.a=1.X?", true }, //
							{ "(^)?.X?.a=2.X?", false }, //
							{ "(^)?.X?.=1.X?", false }, //
					}, //
			}, insomnia.implem.fsa.fpa.TestAutomaton.matchData() //
			));

		List<Object[]> tmp = new ArrayList<>(), ret = new ArrayList<>();

		for (Object[][] item : a)
		{
			Object[] regex = item[0];
			tmp.addAll(mergeParameters(Collections.singletonList(regex), Arrays.asList(ArrayUtils.subarray(item, 1, item.length))));
		}

		for (Object[] item : tmp)
		{
			for (ITree<String, String> t : Trees.treesFromString((String) item[1]))
			{
				item[1] = t;
				ret.add(item.clone());
			}
		}
		return ret;
	}

	@ParameterizedTest
	@MethodSource
	void match(String pattern, ITree<String, String> tree, boolean match) throws ParseException
	{
		assumeTrue(new PRegexParser("''\"\"~~").parse(pattern).size() == 1);
		IBUFTA<String, String> bufta = new BUBuilder<>(Trees.treeFromString(pattern)).create();
		assertEquals(match, bufta.matcher(tree).matches());
	}

	static List<Object[]> find()
	{
		/*
		 * searchIn -> { searchFor }
		 */
		List<Object[][]> a = //
			Arrays.asList(new Object[][][] { //
					{ { "X.a(b.X,c.X)" }, //
							{ "a", 1 }, //
							{ "a(b,c)", 1 }, //
							{ "a.b", 1 }, //
							{ "a.d", 0 }, //
							{ "^a", 0 }, //
							{ "b$", 0 }, //
					}, //
					{ { "a.X,b.X" }, //
							{ "a", 1 }, //
							{ "a,b", 1 }, //
							{ "^a", 0 }, //
					}, //
					{ { "(a(a(a,b),b))" }, //
							{ "a", 3 }, //
							{ "a,b", 2 }, //
							{ "a.b", 2 }, //
							{ "a.a.a", 1 }, //
							{ "a.a.b", 1 }, //
							{ "a.a.c", 0 }, //
							{ "^a", 0 }, //
					}, //
					{ { "^a(a(b,c),b,c$)" }, //
							{ "a", 2 }, //
							{ "^a", 1 }, //
							{ "b", 2 }, //
							{ "^b", 0 }, //
							{ "c", 2 }, //
							{ "^c", 0 }, //
							{ "c$", 1 }, //
							{ "a.c", 2 }, //
							{ "a.c$", 1 }, //
							{ "^a.c$", 1 }, //
							{ "a(b,c)", 2 }, //
							{ "^a(b,c)", 1 }, //
							{ "a.a", 1 }, //
							{ "^a.a", 1 }, //
					}, //
					{ { "=1" }, //
							{ "=1", 1 }, //
							{ "^=1", 0 }, //
							{ "=1$", 0 }, //
							{ "^=1$", 0 }, //
							{ "=2", 0 }, //
					}, //
					{ { "^=1$" }, //
							{ "=1", 1 }, //
							{ "^=1", 1 }, //
							{ "=1$", 1 }, //
							{ "^=1$", 1 }, //
							{ "=2", 0 }, //
					}, //
					{ { "^=1.(b=2,c=1(a,b=2,x=1))" }, //
							{ "=1", 3 }, //
							{ "=2", 2 }, //
							{ "=3", 0 }, //
							{ "^=1", 1 }, //
							{ "b=2", 2 }, //
							{ "^b=2", 1 }, //
							{ "b=1", 0 }, //
					}, //
			});

		List<Object[]> ret = new ArrayList<>();

		for (Object[][] item : a)
		{
			Object[] tree = item[0];
			ret.addAll(mergeParameters(Collections.singletonList(tree), Arrays.asList(ArrayUtils.subarray(item, 1, item.length))));
		}
		return ret;
	}

	@ParameterizedTest
	@MethodSource
	void find(String searchIn, String searchFor, int nb) throws ParseException
	{
		ITree<String, String> pattern, element;
		element = Trees.treeFromString(searchIn);
		pattern = Trees.treeFromString(searchFor);

		IBUFTA<String, String>       bufta   = new BUBuilder<>(pattern).create();
		ITreeMatcher<String, String> matcher = bufta.matcher(element);

		int i = 0;

		assertEquals(nb > 0, matcher.matches());
		while (matcher.find())
		{
			i++;
			ITree<String, String> group = matcher.toMatchResult().group();

			assertTrue(ITree.projectEquals(pattern, group), //
				String.format("Expected \n%s; but have\n%s", ITree.toString(pattern), ITree.toString(group)));
			assertTrue(ITree.isSubTreeOf(matcher.toMatchResult().group(), element), //
				String.format("Expected\n%s to be a subtree of\n %s", ITree.toString(group), element));
		}
		assertEquals(nb, i);
	}
}