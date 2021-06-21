package insomnia.implem.fsa.fta;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.HelpTests;
import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatchResult;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fta.IBUFTA;
import insomnia.implem.data.Trees;
import insomnia.implem.data.regex.TreeMatchResultIterator;
import insomnia.implem.data.regex.parser.RegexParser;
import insomnia.implem.fsa.fta.creational.BUFTABuilder;

public class TestAutomaton
{
	// ==========================================================================

	public static Stream<Object[]> arguments()
	{
		return HelpTests.loadResourceCSVAsMap(//
			"/insomnia/implem/fsa/searchForIn.csv" //
			, "/insomnia/implem/fsa/searchInFor.csv" //
			, "/insomnia/implem/fsa/searchForIn_extended.csv" //
		).map(record -> {
			try
			{
				String searchFor = record.get("search_for");
				String searchIn  = record.get("search_in");
				int    nbMatches = Integer.parseInt(record.get("nb_matches"));

				return new Object[] { searchFor, searchIn, nbMatches };
			}
			catch (Exception e)
			{
				throw new AssertionError(e);
			}
		});
	}

	public static Stream<Object[]> match()
	{
		return arguments().flatMap(args -> {
			List<ITree<String, String>> searchIn;
			try
			{
				searchIn = Trees.treesFromString((String) args[1]);
				return searchIn.stream().map(sin -> {
					args[1] = sin;
					return args;
				});
			}
			catch (ParseException e)
			{
				throw new AssertionError(e);
			}
		});
	}

	@ParameterizedTest
	@MethodSource
	void match(String searchFor, ITree<String, String> tsearchIn, int nb) throws ParseException
	{
		assumeTrue(new RegexParser("''\"\"~~").parse(searchFor).size() == 1);
		boolean match = nb > 0;

		ITree<String, String> tsearchFor = Trees.treeFromString(searchFor);

		IBUFTA<String, String> bufta = new BUFTABuilder<>(tsearchFor).create();

		assertEquals(match, bufta.matcher(tsearchIn).matches(), String.format("Search for=\n%sSearch in=\n%s", ITree.toString(tsearchFor), ITree.toString(tsearchIn)));
	}

	@ParameterizedTest
	@MethodSource("match")
	void find(String searchFor, ITree<String, String> tsearchIn, int nb) throws ParseException
	{
		assumeTrue(new RegexParser("''\"\"~~").parse(searchFor).size() == 1);
		ITree<String, String> tsearchFor = Trees.treeFromString(searchFor);

		IBUFTA<String, String>       bufta   = new BUFTABuilder<>(tsearchFor).create();
		ITreeMatcher<String, String> matcher = bufta.matcher(tsearchIn);

		List<ITreeMatchResult<String, String>> results = IteratorUtils.toList(new TreeMatchResultIterator<>(matcher));
		assertEquals(nb, results.size(), String.format("Expected to find %d match(es), founded: %d\nSearch for=\n%sSearch in=\n%s\nResults=\n%s", nb, results.size(), ITree.toString(tsearchFor), ITree.toString(tsearchIn), results));

		for (var result : results)
		{
			ITree<String, String> group = result.group();

			assertTrue(ITree.structProject(tsearchFor, group), //
				String.format("Expected \n%s; but have\n%s", ITree.toString(tsearchFor), ITree.toString(group)));
			assertTrue(ITree.isSubTreeOf(group, tsearchIn), //
				String.format("Expected\n%s to be a subtree of\n %s", ITree.toString(group), ITree.toString(tsearchIn)));
		}
	}
}