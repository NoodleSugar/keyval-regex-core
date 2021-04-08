package insomnia.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.HelpTests;
import insomnia.implem.data.Trees;

public class TestTree
{
	private static Stream<Map<String, String>> arguments()
	{
		return HelpTests.loadResourceCSVAsMap(//
			"/insomnia/data/project.csv" //
			, "/insomnia/data/equals.csv" //
			, "/insomnia/data/struct.csv" //
			, "/insomnia/data/semi_twig.csv" //
		);
	}

	public static Stream<Object[]> testOf(String what)
	{
		return arguments().flatMap(args -> {

			if (!args.containsKey(what))
				return Stream.empty();

			List<ITree<String, String>> from, to;
			try
			{
				from = Trees.treesFromString(args.get("from"));
				to   = Trees.treesFromString(args.get("to"));
				int i = Integer.parseInt(args.get(what));

				if (i < 0)
					return Stream.empty();

				return from.stream().flatMap(f -> to.stream().map(t -> new Object[] { f, t, i }));
			}
			catch (ParseException e)
			{
				throw new AssertionError(e);
			}
		});
	}

	public static Stream<Object[]> booleanTestOf(String what)
	{
		return testOf(what).map(o -> {
			o[2] = ((Integer) o[2]) > 0;
			return o;
		});
	}

	// ==========================================================================

	public static Stream<Object[]> project()
	{
		return booleanTestOf("project");
	}

	@ParameterizedTest
	@MethodSource
	void project(ITree<String, String> from, ITree<String, String> to, boolean project)
	{
		assertEquals(project, ITree.project(from, to), String.format("Expected\n%s to%s be projected on\n%s", from, project ? "" : " not", to));
	}

	// ==========================================================================

	public static Stream<Object[]> structProject()
	{
		return booleanTestOf("struct_project");
	}

	@ParameterizedTest
	@MethodSource
	void structProject(ITree<String, String> from, ITree<String, String> to, boolean project)
	{
		assertEquals(project, ITree.structProject(from, to), String.format("Expected\n%s to%s be structurally projected on\n%s", from, project ? "" : " not", to));
	}

	// ==========================================================================

	public static Stream<Object[]> included()
	{
		return booleanTestOf("included");
	}

	@ParameterizedTest
	@MethodSource
	void included(ITree<String, String> from, ITree<String, String> to, boolean included)
	{
		assertEquals(included, ITree.included(from, to), String.format("Expected\n%s to%s be included in\n%s", from, included ? "" : " not", to));
	}

	// ==========================================================================

	public static Stream<Object[]> equals()
	{
		return booleanTestOf("equals");
	}

	@ParameterizedTest
	@MethodSource
	void equals(ITree<String, String> from, ITree<String, String> to, boolean equals)
	{
		assertEquals(equals, ITree.equals(from, to), String.format("(from=to) Expected\n%s to%s be equal to\n%s", from, equals ? "" : " not", to));
		assertEquals(equals, ITree.equals(to, from), String.format("(to=from) Expected\n%s to%s be equal to\n%s", from, equals ? "" : " not", to));
	}

	// ==========================================================================

	public static Stream<Object[]> structEquals()
	{
		return booleanTestOf("struct_equals");
	}

	@ParameterizedTest
	@MethodSource
	void structEquals(ITree<String, String> from, ITree<String, String> to, boolean val)
	{
		assertEquals(val, ITree.structEquals(from, to), String.format("Expected\n%s to%s be structurally equal to\n%s", from, val ? "" : " not", to));
	}

	// ==========================================================================

	public static Stream<Object[]> hasSemiTwig()
	{
		return testOf("semi_twig");
	}

	@ParameterizedTest
	@MethodSource

	void hasSemiTwig(ITree<String, String> from, ITree<String, String> stwig, int val)
	{
		var semiTwigs = ITree.getSemiTwigs(from, stwig);
		assertEquals(val, semiTwigs.size(), String.format("from=\n%sstwigs=\n%sFounded %d: %s\n", from, stwig, semiTwigs.size(), semiTwigs));
	}
}
