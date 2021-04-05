package insomnia.implem.fsa.fpa;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.HelpTests;
import insomnia.data.IPath;
import insomnia.data.ITree;
import insomnia.data.regex.IPathMatcher;
import insomnia.fsa.fpa.IFPA;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.Paths;
import insomnia.implem.data.Trees;
import insomnia.implem.data.regex.parser.IRegexElement;
import insomnia.implem.data.regex.parser.RegexParser;
import insomnia.implem.fsa.fpa.creational.FPAFactory;
import insomnia.implem.fsa.fpa.graphchunk.modifier.GCPathRuleApplierSimple;
import insomnia.implem.rule.PathRules;
import insomnia.implem.rule.dependency.BetaDependencyValidation;
import insomnia.implem.rule.grd.creational.GRDFactory;
import insomnia.implem.unifier.PathUnifier;
import insomnia.rule.IRule;
import insomnia.rule.grd.IGRD;
import insomnia.unifier.PathUnifiers;

public class TestAutomaton
{
	static FPAFactory<String, String> fpaFactory(IRegexElement e)
	{
		return new FPAFactory<>(e, s -> s, s -> s);
	}

	static FPAFactory<String, String> fpaFactory(IPath<String, String> path)
	{
		return new FPAFactory<>(path, s -> s, s -> s);
	}

	static IPath<String, String> pathFromString(String path)
	{
		RegexParser parser = new RegexParser(Collections.emptyMap());
		try
		{
			return Paths.pathFromPRegexElement(parser.parse(path), s -> s, s -> s);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	static IRule<String, String> ruleFromStrings(String body, String head)
	{
		try
		{
			return PathRules.fromString(body, head);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	static IRule<String, String> ruleFromStrings(String body, String head, boolean isExistential)
	{
		try
		{
			return PathRules.fromString(body, head, isExistential);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	static private interface ITestFPAFactory
	{
		IGFPA<String, String> parse(IRegexElement e);

		IGFPA<String, String> parse(IPath<String, String> e);
	}

	static Stream<ITestFPAFactory> factories()
	{
		ITestFPAFactory ret[] = { //
				new ITestFPAFactory()
				{
					@Override
					public IGFPA<String, String> parse(IRegexElement e)
					{
						return fpaFactory(e).create();
					}

					@Override
					public IGFPA<String, String> parse(IPath<String, String> e)
					{
						return fpaFactory(e).create();
					}

					public String toString()
					{
						return "Direct";
					}
				}, //
				new ITestFPAFactory()
				{
					@Override
					public IGFPA<String, String> parse(IRegexElement e)
					{
						return fpaFactory(e).createBuilder().create();
					}

					@Override
					public IGFPA<String, String> parse(IPath<String, String> e)
					{
						return fpaFactory(e).createBuilder().create();
					}

					public String toString()
					{
						return "Builder";
					}
				}, //
				new ITestFPAFactory()
				{
					@Override
					public IGFPA<String, String> parse(IRegexElement e)
					{
						return fpaFactory(e).createBuilder().createNewStates(true).create();
					}

					@Override
					public IGFPA<String, String> parse(IPath<String, String> e)
					{
						return fpaFactory(e).createBuilder().createNewStates(true).create();
					}

					public String toString()
					{
						return "Builder new states";
					}
				}, //
//				new ITestFPAFactory()
//				{
//
//					@Override
//					public IGFPA<String, String> parse(IRegexElement e)
//					{
//						return fpaFactory(e).createBuilder().mustBeSync(true).create();
//					}
//
//					@Override
//					public IGFPA<String, String> parse(IPath<String, String> e)
//					{
//						return fpaFactory(e).createBuilder().mustBeSync(true).create();
//					}
//
//					public String toString()
//					{
//						return "Builder sync";
//					}
//				}, //
		};
		return Stream.of(ret);
	}

	static Stream<Object[]> mergeFactories(Supplier<Stream<Object[]>> data)
	{
		return factories().flatMap(f -> data.get().map(d -> ArrayUtils.insert(0, d, (Object) f)));
	}

	static RegexParser parser;

	{
		Map<String, String> valueDelimiters = new HashMap<>();
		valueDelimiters.put("'", "'");
		valueDelimiters.put("~", "~");
		parser = new RegexParser(valueDelimiters);
	}

	// =========================================================================

	static Stream<Object[]> arguments()
	{
		return mergeFactories(() -> HelpTests.loadResourceCSVAsMap(//
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
		}));
	}

	static Stream<Object[]> match()
	{
		return arguments().flatMap(args -> {
			List<ITree<String, String>> searchIn;
			try
			{
				searchIn = Trees.treesFromString((String) args[2]);
				return searchIn.stream().map(sin -> {
					args[2] = sin;
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
	void match(ITestFPAFactory fpaFactory, String searchFor, ITree<String, String> tsearchIn, int nb) throws ParseException
	{
		IRegexElement esearchFor = Trees.getParser().parse(searchFor);
		assumeTrue(esearchFor.isPath());
		assumeTrue(tsearchIn.isPath());
		boolean match = nb > 0;

		IFPA<String, String> automaton = fpaFactory.parse(esearchFor);
		assertEquals(match, automaton.matcher((IPath<String, String>) tsearchIn).matches());
	}

	// ==========================================================================

	static Stream<Object[]> find()
	{
		return arguments().flatMap(args -> {
			List<ITree<String, String>> searchIn;
			try
			{
				IRegexElement esearchFor = Trees.getParser().parse((String) args[1]);

				// Can't handle other size for tests because of 'nb_matches' argument
				if (esearchFor.size() != 1)
					return Stream.empty();

				var searchForList = Trees.treesFromPRegexElement(esearchFor, Function.identity(), Function.identity());
				searchIn = Trees.treesFromString((String) args[2]);

				return searchIn.stream().flatMap(sin -> searchForList.stream().map(sfor -> {
					args[1] = sfor;
					args[2] = sin;
					return args;
				}));
			}
			catch (ParseException e)
			{
				throw new AssertionError(e);
			}
		});
	}

	@ParameterizedTest
	@MethodSource
	void find(ITestFPAFactory fprovider, ITree<String, String> tsearchFor, ITree<String, String> tsearchIn, int nb) throws ParseException, IOException
	{
		assumeTrue(tsearchFor.isPath());
		assumeTrue(tsearchIn.isPath());
		IPath<String, String>        psearchIn  = (IPath<String, String>) tsearchIn;
		IPath<String, String>        psearchFor = (IPath<String, String>) tsearchFor;
		IPathMatcher<String, String> matcher    = fprovider.parse(psearchFor).matcher(psearchIn);

		int i = 0;
		while (matcher.find())
		{
			IPath<String, String> group = matcher.toMatchResult().group();

			assertTrue(ITree.structProject(psearchFor, group), //
				String.format("Expected\n%s but have\n%s", ITree.toString(psearchFor), ITree.toString(group)));

			assertTrue(ITree.isSubTreeOf(group, psearchIn), //
				String.format("Expected\n%s to be a subtree of\n %s", ITree.toString(group), ITree.toString(psearchIn)));
			i++;
		}
		if (nb == 0)
			assertEquals(0, i);
		else
			assertTrue(nb == i, String.format("Expected %d == %d\n", nb, i));
	}

	// =========================================================================

	/**
	 * Information for a test on path rewriting.
	 * 
	 * @author zuri
	 */
	private interface PathRewriting_data
	{
		Collection<IRule<String, String>> getRules();

		List<Object[]> getTestObjects();

	};

	static PathRewriting_data prd1()
	{
		return new PathRewriting_data()
		{
			@Override
			public Collection<IRule<String, String>> getRules()
			{
				return Arrays.asList( //
					ruleFromStrings("x", "c"), //
					ruleFromStrings("c.h", "h"), //
					ruleFromStrings("z$", "x", true), //
					ruleFromStrings("y", "b.x", true) //
				);
			}

			@Override
			public List<Object[]> getTestObjects()
			{
				return Arrays.asList(new Object[][] { //
						{ 3, "a.b.c", new Object[][] { //
								{ "a.b.c", true }, //
								{ "a.b.x", true }, //

								{ "a.y", true }, //
								{ "a.y$", true }, //
								{ "a.y.b.b.b", true }, //

								{ "a.b.z$", true }, //
								{ "a.b.z", false }, //
								{ "a.b.z.a", false }, //

								{ "a.b", false }, //
								{ "a.b.c.c", true }, //
								{ "a.b.d", false }, //
						} }, //
						{ 3, "^.h$", new Object[][] { //
								{ "^.h$", true }, //
								{ "^.c.h$", true }, //
								{ "^.x.h$", true }, //
								{ "^.c.c.h$", true }, //
								{ "^.x.c.h$", true }, //
								{ "^.x.x.h$", true }, //

								{ "^.z$", true }, //
								{ "^.x.z$", true }, //

								{ "^.a$", false }, //
								{ "^.x.x.a$", false }, //
						} }, //
				});
			}
		};
	}

	static List<Object[]> pathRewriting()
	{
		PathRewriting_data datas[] = { prd1() };
		List<Object[]>     ret     = new ArrayList<>();

		for (PathRewriting_data data : Arrays.asList(datas))
		{
			for (Object[] dataObjects : data.getTestObjects())
			{
				for (Object[] expected : (Object[][]) dataObjects[2])
				{
					List<Object> items = new ArrayList<>();
					items.add(data.getRules());

					// Max depth
					items.add(dataObjects[0]);
					// The query
					items.add(dataObjects[1]);

					// Expected
					items.addAll(Arrays.asList(expected));

					ret.add(items.toArray());
				}
			}
		}
		return ret;
	}

	@ParameterizedTest
	@MethodSource
	void pathRewriting(Collection<IRule<String, String>> rules, int maxDepth, String regex, String query, boolean expected) throws ParseException, NoSuchMethodException, SecurityException
	{
		GCPathRuleApplierSimple<String, String> modifier = new GCPathRuleApplierSimple<>(maxDepth);
		IGRD<String, String>                    grd      = new GRDFactory<>(rules, new BetaDependencyValidation<>(new PathUnifiers(PathUnifier.class))).create();

		IFPA<String, String> automaton = fpaFactory(Paths.pathFromString(regex)) //
			.setGraphChunkModifier(modifier.getGraphChunkModifier(grd)) //
			.create();

		assertEquals(expected, automaton.matcher(Paths.pathFromString(query)).matches());
	}
}