package insomnia.implem.fsa.fpa;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.IPath;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fpa.IFPA;
import insomnia.implem.fsa.fpa.creational.FPAFactory;
import insomnia.implem.fsa.fpa.graphchunk.modifier.GCPathRuleApplierSimple;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVLabels;
import insomnia.implem.kv.data.KVPaths;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.data.KVValues;
import insomnia.implem.kv.pregex.IPRegexElement;
import insomnia.implem.kv.pregex.PRegexParser;
import insomnia.implem.kv.rule.KVPathRules;
import insomnia.implem.kv.unifier.KVPathUnifiers;
import insomnia.implem.rule.dependency.BetaDependencyValidation;
import insomnia.implem.rule.grd.creational.GRDFactory;
import insomnia.lib.help.HelpLists;
import insomnia.rule.IRule;
import insomnia.rule.grd.IGRD;

public class TestAutomaton
{
	static List<Object[]> factories()
	{
		@SuppressWarnings("rawtypes")
		Object ret[][] = { //
				{ "FPAFactory", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return new FPAFactory<KVValue, KVLabel>((IPRegexElement) obj, KVLabels.getFSALabelFactory()).create();
					}

				} }, //
				{ "FromBuilder", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return new FPAFactory<KVValue, KVLabel>((IPRegexElement) obj, KVLabels.getFSALabelFactory()).createBuilder().create();
					}

				} }, //
				{ "FromBuilder newStates", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return new FPAFactory<KVValue, KVLabel>((IPRegexElement) obj, KVLabels.getFSALabelFactory()).createBuilder().createNewStates(true).create();
					}

				} }, //
				{ "FromBuilderSync", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return new FPAFactory<KVValue, KVLabel>((IPRegexElement) obj, KVLabels.getFSALabelFactory()).createBuilder().mustBeSync(true).create();
					}

				} } //
		};
		return Stream.of(ret).collect(Collectors.toList());
	}

	static List<Object[]> mergeParameters(List<Object[]> a, List<Object[]> b)
	{
		List<Pair<Object[], Object[]>> ret    = HelpLists.product(a, b);
		List<Object[]>                 merged = HelpLists.mergePairsArrays(ret);
		return merged;
	}

	private static IFPA<KVValue, KVLabel> parse(String regex, Function<IPRegexElement, IFPA<KVValue, KVLabel>> automatonFactoryProvider) throws IOException, ParseException
	{
		return parse(regex, automatonFactoryProvider, null);
	}

	private static IFPA<KVValue, KVLabel> parse(String regex, Function<IPRegexElement, IFPA<KVValue, KVLabel>> automatonFactoryProvider, KVValue value) throws IOException, ParseException
	{
		IPRegexElement rparsed = new PRegexParser().parse(IOUtils.toInputStream(regex, Charset.defaultCharset()), value);
		return automatonFactoryProvider.apply(rparsed);
	}

	private static IFPA<KVValue, KVLabel> automatonFromPath(IPath<KVValue, KVLabel> path)
	{
		return new FPAFactory<>(path).create();
	}

	// =========================================================================

	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	class subTests
	{
		IFPA<KVValue, KVLabel> automaton;

		@BeforeAll
		void setup() throws IOException, ParseException
		{
			String         regex   = "a*.b?.c+|(d.(e|f){2,5}).~r*e?g+~";
			IPRegexElement rparsed = new PRegexParser().parse(IOUtils.toInputStream(regex, Charset.defaultCharset()));
			automaton = new FPAFactory<KVValue, KVLabel>(rparsed, KVLabels.getFSALabelFactory()).create();
		}

		List<Object[]> complex()
		{
			List<Object[]> a = Arrays.asList(new Object[][] { //
					{ "a.d.e.e.g", false }, //
					{ "a.b.c", true }, //
					{ "b.c.c.c.c.c", true }, //
					{ "a.a.a.c.c", true }, //
					{ "c", true }, //
					{ "d.e.f.e.e.f.reg", true }, //
					{ "d.f.f.gggg", true }, //
					{ "d.e.e.rrrrrrreg", true }, //
					{ "c", true }, //

					{ "a.b.b.c", false }, //
					{ "a.b", false }, //
					{ "d.e.reg", false }, //
					{ "e.e.reg", false }, //
					{ "d.f.f.re", false }, //
					{ "a.b.c.e", false }, //
			});
			return a;
		}

		@ParameterizedTest
		@MethodSource
		void complex(String subject, boolean match)
		{
			assertEquals(match, automaton.matcher(KVPaths.pathFromString(subject)).matches());
		}
	}
	// =========================================================================

	static List<Object[]> match()
	{
		List<Object[]> a = Arrays.asList(new Object[][] { //
				{ "a", "a", true }, //
				{ "a", "b", false }, //

				{ "a.b", "a.b", true }, //
				{ "a.b", "a.c", false }, //

				{ "a|b", "a", true }, //
				{ "a|b", "b", true }, //
				{ "a|b", "c", false }, //

				{ "a|b.c", "a", true }, //
				{ "a|b.c", "b.c", true }, //
				{ "a|b.c", "b", false }, // jun

				{ "(a|b).c", "a.c", true }, //
				{ "(a|b).c", "b.c", true }, //
				{ "(a|b).c", "a", false }, //
				{ "(a|b).c", "b", false }, //
				{ "(a|b).c", "c", false }, //

				{ "a{3}", "a.a.a", true }, //
				{ "a{3}", "b.a.a.a", false }, //
				{ "a{3}", "a", false }, //

				{ "a{1,2}", "a", true }, //
				{ "a{1,2}", "a.a", true }, //
				{ "a{1,2}", "a.a.a", false }, //

				{ "a{0,1}", "", true }, //
				{ "a{0,1}", "a", true }, //
				{ "a{0,1}", "a.a", false }, //

				{ "a*", "", true }, //
				{ "a*", "a", true }, //
				{ "a*", "a.a", true }, //
				{ "a*", "a" + StringUtils.repeat(".a", 100), true }, //
				{ "a*", "a.b.a.a.a", false }, //

				{ "a+", "", false }, //
				{ "a+", "a", true }, //
				{ "a+", "a" + StringUtils.repeat(".a", 100), true }, //

				{ "a?", "", true }, //
				{ "a?", "a", true }, //
				{ "a?", "a.a", false }, //

				{ "a*.b|x", "a.x", false }, //
				{ "x.a+|y.b+", "x.a.b", false }, //

//				{ "a.b.c.", "a.b.c", false }, //
		});
		return mergeParameters(factories(), a);
	}

	@ParameterizedTest
	@MethodSource
	void match(String fname, Function<IPRegexElement, IFPA<KVValue, KVLabel>> fprovider, String regex, String pathSubject, boolean match)
	{
		try
		{
			IFPA<KVValue, KVLabel> automaton = parse(regex, fprovider);
			assertEquals(match, automaton.matcher(KVPaths.pathFromString(pathSubject)).matches());
		}
		catch (IOException | ParseException e)
		{
			fail(e.getMessage());
		}
	}

	// =========================================================================

	static List<Object[]> matchValue()
	{
		List<Object[]> a = Arrays.asList(new Object[][] { //
				{ "a.b", KVValues.create(15), KVPaths.pathFromString("a.b", KVValues.create(15)), true }, //
				{ "a.b", KVValues.create(15), KVPaths.pathFromString("a.b", KVValues.create(16)), false }, //
				{ "a.b", KVValues.create(15), KVPaths.pathFromString("a.b"), false }, //
		});
		return mergeParameters(factories(), a);
	}

	@ParameterizedTest
	@MethodSource
	void matchValue(String fname, Function<IPRegexElement, IFPA<KVValue, KVLabel>> fprovider, String regex, KVValue rvalue, IPath<KVValue, KVLabel> subject, boolean match)
	{
		try
		{
			IFPA<KVValue, KVLabel> automaton = parse(regex, fprovider, rvalue);
			assertEquals(match, automaton.matcher(subject).matches());
		}
		catch (IOException | ParseException e)
		{
			fail(e.getMessage());
		}
	}

	// =========================================================================

	static List<Object[]> matchPath()
	{
		List<Object[]> a = Arrays.asList(new Object[][] { //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a.b", KVValues.create(15)), true }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a.b"), true }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a.a.b"), true }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a.b.c"), true }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a.a.b.c"), true }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a"), false }, //

				// Prefix
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a.b", KVValues.create(15)), true }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a.b.c", KVValues.create(15)), true }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a.b"), true }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a.b.c"), true }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a.a.b"), false }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a"), false }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString("a.b"), false }, //

				// Suffix
				{ KVPaths.pathFromString("a.b", KVValues.create(15)), KVPaths.pathFromString("a.b", KVValues.create(15)), true }, //
				{ KVPaths.pathFromString("a.b", KVValues.create(15)), KVPaths.pathFromString("a.b"), false }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.b.", KVValues.create(15)), true }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.b."), true }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.a.b."), true }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.a.b.", KVValues.create(15)), true }, //

				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.b.c."), false }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a"), false }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.b"), false }, //

				// Complete
				{ KVPaths.pathFromString(".a.b", KVValues.create(15)), KVPaths.pathFromString(".a.b", KVValues.create(15)), true }, //
				{ KVPaths.pathFromString(".a.b", KVValues.create(15)), KVPaths.pathFromString(".a.b"), false }, //
		});
		return a;
	}

	@ParameterizedTest
	@MethodSource
	void matchPath(IPath<KVValue, KVLabel> path, IPath<KVValue, KVLabel> query, boolean match)
	{
		IFPA<KVValue, KVLabel> automaton = automatonFromPath(path);
		assertEquals(match, automaton.matcher(query).matches());
	}

	static List<Object[]> findPath()
	{
		return Arrays.asList(new Object[][] { //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a.b.c.a.b.a"), new int[] { 0, 3 } }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString(".a.b.c.a.b.a"), new int[] { 1, 4 } }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString("a.b.c.a.b.a.b."), new int[] { 0, 3, 5 } }, //
				{ KVPaths.pathFromString("a.b"), KVPaths.pathFromString(".a.b.c.a.b.a.b."), new int[] { 1, 4, 6 } }, //

				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString("a.b.c.a.b.a"), new int[] {} }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a.b.c.a.b.a"), new int[] { 0 } }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString("a.b.c.a.b.a.b."), new int[] {} }, //
				{ KVPaths.pathFromString(".a.b"), KVPaths.pathFromString(".a.b.c.a.b.a.b."), new int[] { 0 } }, //

				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.b.c.a.b.a.b"), new int[] {} }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString("a.b.c.a.b.a.b."), new int[] { 5 } }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString(".a.b.c.a.b.a.b"), new int[] {} }, //
				{ KVPaths.pathFromString("a.b."), KVPaths.pathFromString(".a.b.c.a.b.a.b."), new int[] { 6 } }, //

				{ KVPaths.pathFromString(".a.b."), KVPaths.pathFromString("a.b"), new int[] {} }, //
				{ KVPaths.pathFromString(".a.b."), KVPaths.pathFromString(".a.b"), new int[] {} }, //
				{ KVPaths.pathFromString(".a.b."), KVPaths.pathFromString("a.b."), new int[] {} }, //
				{ KVPaths.pathFromString(".a.b."), KVPaths.pathFromString(".a.b."), new int[] { 0 } }, //

				{ KVPaths.pathFromString(".a.b."), KVPaths.pathFromString(".a.b.b."), new int[] {} }, //
				{ KVPaths.pathFromString(".a.b."), KVPaths.pathFromString(".a.a.b."), new int[] {} }, //
		});
	}

	@ParameterizedTest
	@MethodSource
	void findPath(IPath<KVValue, KVLabel> path, IPath<KVValue, KVLabel> query, int groups[])
	{
		IFPA<KVValue, KVLabel>         automaton = automatonFromPath(path);
		ITreeMatcher<KVValue, KVLabel> matcher   = automaton.matcher(query);
		int                            rooted    = BooleanUtils.toInteger(path.isRooted());
		int                            i         = 0;

		while (matcher.find())
		{
			assertEquals(query.subPath(groups[i], groups[i] + path.nbLabels() + rooted), matcher.group());
			i++;
		}
		assertEquals(groups.length, i);
	}

	// =========================================================================

	/**
	 * Information for a test on path rewriting.
	 * 
	 * @author zuri
	 */
	private interface PathRewriting_data
	{
		Collection<IRule<KVValue, KVLabel>> getRules();

		List<Object[]> getTestObjects();

	};

	static PathRewriting_data prd1()
	{
		return new PathRewriting_data()
		{
			@Override
			public Collection<IRule<KVValue, KVLabel>> getRules()
			{
				return Arrays.asList( //
					KVPathRules.fromString("x", "c"), //
					KVPathRules.fromString("c.h", "h"), //
					KVPathRules.fromString("z.", "x", true), //
					KVPathRules.fromString("y", "b.x", true) //
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
								{ "a.y.", true }, //
								{ "a.y.b.b.b", true }, //

								{ "a.b.z.", true }, //
								{ "a.b.z", false }, //
								{ "a.b.z.a", false }, //

								{ "a.b", false }, //
								{ "a.b.c.c", true }, //
								{ "a.b.d", false }, //
						} }, //
						{ 3, ".h.", new Object[][] { //
								{ ".h.", true }, //
								{ ".c.h.", true }, //
								{ ".x.h.", true }, //
								{ ".c.c.h.", true }, //
								{ ".x.c.h.", true }, //
								{ ".x.x.h.", true }, //

								{ ".z.", true }, //
								{ ".x.z.", true }, //

								{ ".a.", false }, //
								{ ".x.x.a.", false }, //
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
	void pathRewriting(Collection<IRule<KVValue, KVLabel>> rules, int maxDepth, String regex, String query, boolean expected) throws IOException, ParseException
	{
		GCPathRuleApplierSimple<KVValue, KVLabel> modifier = new GCPathRuleApplierSimple<>(maxDepth);
		IGRD<KVValue, KVLabel>                    grd      = new GRDFactory<>(rules, new BetaDependencyValidation<>(KVPathUnifiers.get())).create();

		IFPA<KVValue, KVLabel> automaton = new FPAFactory<KVValue, KVLabel>(KVPaths.pathFromString(regex)) //
			.setGraphChunkModifier(modifier.getGraphChunkModifier(grd)) //
			.create();

		assertEquals(expected, automaton.matcher(KVPaths.pathFromString(query)).matches());
	}
}
