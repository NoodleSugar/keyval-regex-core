package insomnia.implem.fsa.fta;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fta.IBUFTA;
import insomnia.implem.data.Trees;
import insomnia.implem.data.regex.parser.PRegexParser;
import insomnia.implem.fsa.fta.creational.BUBuilder;

public class TestAutomaton
{
	// ==========================================================================

	static Stream<Object[]> arguments() throws ParseException
	{
		return searchForIn(ArrayUtils.addAll(insomnia.implem.fsa.fpa.TestAutomaton.allSearchForIn()));
	}

	public static Stream<Object[]> searchForIn(Object[][][] arrays) throws ParseException
	{
		return Arrays.stream(ArrayUtils.addAll(arrays)) //
			.flatMap((item) -> {
				List<Object[]>   ret       = new ArrayList<>();
				Iterator<Object[]> it      = IteratorUtils.arrayIterator(item);
				String           searchFor = (String) it.next()[0];

				while (it.hasNext())
				{
					Object[]                  subItem = it.next();
					List<ITree<String, String>> paths;
					try
					{
						paths = Trees.treesFromString((String) subItem[0]);

						for (ITree<?, ?> path : paths)
							ret.add(new Object[] { searchFor, path, subItem[1] });
					}
					catch (ParseException e)
					{
						throw new AssertionError(String.format("Error for: %s", subItem[0]), e);
					}
				}
				return ret.stream();
			});
	}

	@ParameterizedTest
	@MethodSource("arguments")
	void match(String searchFor, ITree<String, String> tsearchIn, int nb) throws ParseException
	{
		assumeTrue(new PRegexParser("''\"\"~~").parse(searchFor).size() == 1);
		boolean match = nb > 0;

		ITree<String, String>  tsearchFor = Trees.treeFromString(searchFor);
		IBUFTA<String, String> bufta      = new BUBuilder<>(tsearchFor).create();
		assertEquals(match, bufta.matcher(tsearchIn).matches(), String.format("Search for=\n%sSearch in=\n%s", ITree.toString(tsearchFor), ITree.toString(tsearchIn)));
	}

	@ParameterizedTest
	@MethodSource("arguments")
	void find(String searchFor, ITree<String, String> tsearchIn, int nb) throws ParseException
	{
		assumeTrue(new PRegexParser("''\"\"~~").parse(searchFor).size() == 1);
		ITree<String, String> tsearchFor;
		tsearchFor = Trees.treeFromString(searchFor);

		IBUFTA<String, String>       bufta   = new BUBuilder<>(tsearchFor).create();
		ITreeMatcher<String, String> matcher = bufta.matcher(tsearchIn);

		int i = 0;

		while (matcher.find())
		{
			i++;
			ITree<String, String> group = matcher.toMatchResult().group();

			assertTrue(ITree.projectEquals(tsearchFor, group), //
				String.format("Expected \n%s; but have\n%s", ITree.toString(tsearchFor), ITree.toString(group)));
			assertTrue(ITree.isSubTreeOf(group, tsearchIn), //
				String.format("Expected\n%s to be a subtree of\n %s", ITree.toString(group), ITree.toString(tsearchIn)));
		}
		assertEquals(nb, i, String.format("Expected to find %d match(es), founded: %d\nSearch for=\n%sSearch in=\n%s", nb, i, ITree.toString(tsearchFor), ITree.toString(tsearchIn)));
	}
}