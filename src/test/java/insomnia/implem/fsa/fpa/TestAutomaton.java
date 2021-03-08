package insomnia.implem.fsa.fpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.IPath;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fpa.IFPA;
import insomnia.implem.data.Paths;
import insomnia.implem.data.regex.parser.IPRegexElement;
import insomnia.implem.data.regex.parser.PRegexParser;
import insomnia.implem.fsa.fpa.creational.FPAFactory;
import insomnia.implem.fsa.fpa.graphchunk.modifier.GCPathRuleApplierSimple;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVLabels;
import insomnia.implem.kv.data.KVPaths;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.data.KVValues;
import insomnia.implem.kv.rule.KVPathRules;
import insomnia.implem.kv.unifier.KVPathUnifiers;
import insomnia.implem.rule.dependency.BetaDependencyValidation;
import insomnia.implem.rule.grd.creational.GRDFactory;
import insomnia.lib.help.HelpLists;
import insomnia.rule.IRule;
import insomnia.rule.grd.IGRD;

public class TestAutomaton
{
	static FPAFactory<KVValue, KVLabel> fpaFactory(IPRegexElement e)
	{
		return new FPAFactory<>(e, KVLabels::mapLabel, KVValues::mapValue);
	}

	static FPAFactory<KVValue, KVLabel> fpaFactory(IPath<KVValue, KVLabel> path)
	{
		return new FPAFactory<>(path, KVLabels::mapLabel, KVValues::mapValue);
	}

	static IPath<KVValue, KVLabel> pathFromString(String path)
	{
		PRegexParser parser = new PRegexParser(Collections.emptyMap());
		try
		{
			return Paths.pathFromPRegexElement(parser.parse(path), KVValues::mapValue, KVLabels::mapLabel);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	static IRule<KVValue, KVLabel> ruleFromStrings(String body, String head)
	{
		try
		{
			return KVPathRules.fromString(body, head);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	static IRule<KVValue, KVLabel> ruleFromStrings(String body, String head, boolean isExistential)
	{
		try
		{
			return KVPathRules.fromString(body, head, isExistential);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	static List<Object[]> factories()
	{
		@SuppressWarnings("rawtypes")
		Object ret[][] = { //
				{ "FPAFactory", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return fpaFactory((IPRegexElement) obj).create();
					}

				} }, //
				{ "FromBuilder", new Function()
				{

					@Override
					public Object apply(Object obj)
					{
						return fpaFactory((IPRegexElement) obj).createBuilder().create();
					}

				} }, //
				{ "FromBuilder newStates", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return fpaFactory((IPRegexElement) obj).createBuilder().createNewStates(true).create();
					}

				} }, //

				{ "FromBuilderSync", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return fpaFactory((IPRegexElement) obj).createBuilder().mustBeSync(true).create();
					}

				} } //
		};
		return Stream.of(ret).collect(Collectors.toList());
	}

	static PRegexParser parser;

	{
		Map<String, String> valueDelimiters = new HashMap<>();
		valueDelimiters.put("'", "'");
		valueDelimiters.put("~", "~");
		parser = new PRegexParser(valueDelimiters);
	}

	static List<Object[]> mergeParameters(List<Object[]> a, List<Object[]> b)
	{
		List<Pair<Object[], Object[]>> ret    = HelpLists.product(a, b);
		List<Object[]>                 merged = HelpLists.mergePairsArrays(ret);
		return merged;
	}

	private static IFPA<KVValue, KVLabel> parse(String regex, Function<IPRegexElement, IFPA<KVValue, KVLabel>> automatonFactoryProvider) throws IOException, ParseException
	{
		IPRegexElement rparsed = parser.parse(IOUtils.toInputStream(regex, Charset.defaultCharset()));
		return automatonFactoryProvider.apply(rparsed);
	}

	private static IFPA<KVValue, KVLabel> automatonFromPath(IPath<KVValue, KVLabel> path)
	{
		return fpaFactory(path).create();
	}

	// =========================================================================

	public static Object[][][] matchData()
	{
		/*
		 * searchFor -> { searchIn }
		 */
		return new Object[][][] { //
				{ { "" }, //
						{ "", true }, //
						{ "a", true }, //
				}, //
				{ { "^" }, //
						{ "", false }, //
						{ "$", false }, //
						{ "a", false }, //
						{ "a$", false }, //
						{ "^", true }, //
						{ "^$", true }, //
						{ "^.a", true }, //
						{ "^.a.b.c", true }, //
						{ "^.a.b.c$", true }, //
				}, //
				{ { "$" }, //
						{ "", false }, //
						{ "^", false }, //
						{ "a", false }, //
						{ "^.a", false }, //
						{ "$", true }, //
						{ "^$", true }, //
						{ "a$", true }, //
						{ "a.b.c$", true }, //
						{ "^.a.b.c$", true }, //
				}, //
				{ { "^$" }, //
						{ "", false }, //
						{ "^", false }, //
						{ "$", false }, //
						{ "^$", true }, //
						{ "^=1$", true }, //
				}, //
				{ { "^=1$" }, //
						{ "^=1$", true }, //
						{ "^=2$", false }, //
				}, //
				{ { "a" }, //
						{ "a", true }, //
						{ "b", false }, //
						{ "^a", true }, //
						{ "a$", true }, //
						{ "^a$", true }, //
						{ "a=1", true }, //
						{ "^x.x.a=1.y.y$", true }, //
				}, //
				{ { "a.b" }, //
						{ "a.b", true }, //
						{ "a.c", false }, //
				}, //
				{ { "a|b" }, //
						{ "a", true }, //
						{ "b", true }, //
						{ "c", false }, //
				}, //
				{ { "a|b.c" }, //
						{ "a", true }, //
						{ "b.c", true }, //
						{ "b", false }, //
				}, //
				{ { "(a|b).c" }, //
						{ "a.c", true }, //
						{ "b.c", true }, //
						{ "a", false }, //
						{ "b", false }, //
						{ "c", false }, //
						{ "a.b", false }, //
				}, //
				{ { "a{3}" }, //
						{ "a.a.a", true }, //
						{ "b.a.a.a", true }, //
						{ "a", false }, //
				}, //
				{ { "a{1,2}" }, //
						{ "a", true }, //
						{ "a.a", true }, //
						{ "a.a.a", true }, //
				}, //
				{ { "a{0,1}" }, //
						{ "", true }, //
						{ "a", true }, //
						{ "a.a", true }, //
				}, //
				{ { "a*" }, //
						{ "", true }, //
						{ "a", true }, //
						{ "a.a", true }, //
						{ "a" + StringUtils.repeat(".a", 100), true }, //
						{ "a.b.a.a.a", true }, //
				}, //
				{ { "^.a*$" }, //
						{ "^.a.b.a.a.a", false }, //
				}, { { "a+" }, //
						{ "", false }, //
						{ "a", true }, //
						{ "a" + StringUtils.repeat(".a", 100), true }, //
				}, //
				{ { "a?" }, //
						{ "", true }, //
						{ "a", true }, //
						{ "a.a", true }, //
				}, //
				{ { "^.x.a+|y.b+" }, //
						{ "^.x.b", false }, //
						{ "^.x.a.b", true }, //
						{ "^.y.b", true }, //
						{ "^.y.b.b.b.b", true }, //
						{ "^.y.a", false }, //
				}, //
				{ { "=15" }, //
						{ "=15", true }, //
						{ "=16", false }, //
						{ "x=15", true }, //
						{ "x=16", false }, //
						{ "a.b=15", true }, //
						{ "a.b=15.c=16", true }, //
				}, //
				{ { "x=15" }, //
						{ "=15", false }, //
						{ "=16", false }, //
						{ "x=15", true }, //
						{ "x=16", false }, //
						{ "a.x=15", true }, //
						{ "a.x=15.c=16", true }, //
				}, //
				{ { "a.b=15" }, //
						{ "a.b=15", true }, //
						{ "a.b=16", false }, //
						{ "a.b", false }, //
				}, //
				{ { "^.a?$" }, //
						{ "^$", true }, //
						{ "^a$", true }, //
						{ "^a", false }, //
						{ "a$", false }, //
						{ "^a.a$", false }, //
						{ "a", false }, //
						{ "", false }, //
						{ "^", false }, //
						{ "$", false }, //
				}, //
				{ { "^.a*.b|^.x" }, { "^.a.x", false } }, //
				{ { "a*.b?.c*.c$|(^.d.(e|f){2,5}).~r*e?g+~$" }, //
						{ "a.d", false }, //
						{ "a.b.c", false }, //
						{ "a.b.c$", true }, //
						{ "b.c.c.c.c.c", false }, //
						{ "b.c.c.c.c.c$", true }, //
						{ "a.a.a.c.c$", true }, //
						{ "c", false }, //
						{ "c$", true }, //
						{ "^.c$", true }, //

						{ "^.d.e.e.e.e.e.reg$", true }, //
						{ "^.d.e.f.e.e.f.reg$", true }, //
						{ "^.d.f.f.gggg$", true }, //
						{ "^.d.e.e.rrrrrrreg$", true }, //

						{ "a.b.b.c$", true }, //
						{ "a.b", false }, //
						{ "d.e.reg", false }, //
						{ "e.e.reg", false }, //
						{ "d.f.f.re", false }, //
						{ "c.e", false }, //
						{ "a.b.c.e$", false }, //
				} };
	}

	static List<Object[]> match()
	{
		List<Object[][]> a   = Arrays.asList(matchData());
		List<Object[]>   ret = new ArrayList<>();

		for (Object[][] item : a)
		{
			Object[] regex = item[0];
			ret.addAll(mergeParameters(Collections.singletonList(regex), Arrays.asList(ArrayUtils.subarray(item, 1, item.length))));
		}
		return mergeParameters(factories(), ret);
	}

	@ParameterizedTest
	@MethodSource
	void match(String fname, Function<IPRegexElement, IFPA<KVValue, KVLabel>> fprovider, String regex, String pathSubject, boolean match) throws IOException, ParseException
	{
		IFPA<KVValue, KVLabel> automaton = parse(regex, fprovider);
		assertEquals(match, automaton.matcher(KVPaths.pathFromString(pathSubject)).matches());
	}

	// =========================================================================

	static List<Object[]> matchPath()
	{
		List<Object[]> a = Arrays.asList(new Object[][] { //
				{ "a.b", "a.b=15", true }, //
				{ "a.b", "a.b", true }, //
				{ "a.b", "a.a.b", true }, //
				{ "a.b", "a.b.c", true }, //
				{ "a.b", "a.a.b.c", true }, //
				{ "a.b", "a", false }, //

				// Prefix
				{ "^.a.b", "^.a.b=15", true }, //
				{ "^.a.b", "^.a.b.c=15", true }, //
				{ "^.a.b", "^.a.b", true }, //
				{ "^.a.b", "^.a.b.c", true }, //
				{ "^.a.b", "^.a.a.b", false }, //
				{ "^.a.b", "^.a", false }, //
				{ "^.a.b", "a.b", false }, //

				// Suffix
				{ "a.b=15", "a.b=15", true }, //
				{ "a.b=15", "a.b", false }, //
				{ "a.b$", "a.b=15$", true }, //
				{ "a.b$", "a.b$", true }, //
				{ "a.b$", "a.a.b$", true }, //
				{ "a.b$", "a.a.b$", true }, //

				{ "a.b$", "a.b.c$", false }, //
				{ "a.b$", "a", false }, //
				{ "a.b$", "a.b", false }, //

				// Complete
				{ "^.a.b=15", "^.a.b=15", true }, //
				{ "^.a.b=15", "^.a.b", false }, //
		});
		return a;
	}

	@ParameterizedTest
	@MethodSource
	void matchPath(String spath, String squery, boolean match) throws ParseException
	{
		IFPA<KVValue, KVLabel> automaton = automatonFromPath(KVPaths.pathFromString(spath));
		assertEquals(match, automaton.matcher(KVPaths.pathFromString(squery)).matches());
	}

	static List<Object[]> findPath()
	{
		return Arrays.asList(new Object[][] { //
				{ "a.b", "a.b.c.a.b.a", new int[] { 0, 3 } }, //
				{ "a.b", "^.a.b.c.a.b.a", new int[] { 1, 4 } }, //
				{ "a.b", "a.b.c.a.b.a.b$", new int[] { 0, 3, 5 } }, //
				{ "a.b", "^.a.b.c.a.b.a.b$", new int[] { 1, 4, 6 } }, //

				{ "^.a.b", "a.b.c.a.b.a", new int[] {} }, //
				{ "^.a.b", "^.a.b.c.a.b.a", new int[] { 0 } }, //
				{ "^.a.b", "a.b.c.a.b.a.b$", new int[] {} }, //
				{ "^.a.b", "^.a.b.c.a.b.a.b$", new int[] { 0 } }, //

				{ "a.b$", "a.b.c.a.b.a.b", new int[] {} }, //
				{ "a.b$", "a.b.c.a.b.a.b$", new int[] { 5 } }, //
				{ "a.b$", "^.a.b.c.a.b.a.b", new int[] {} }, //
				{ "a.b$", "^.a.b.c.a.b.a.b$", new int[] { 6 } }, //

				{ "^.a.b$", "a.b", new int[] {} }, //
				{ "^.a.b$", "^.a.b", new int[] {} }, //
				{ "^.a.b$", "a.b$", new int[] {} }, //
				{ "^.a.b$", "^.a.b$", new int[] { 0 } }, //

				{ "^.a.b$", "^.a.b.b$", new int[] {} }, //
				{ "^.a.b$", "^.a.a.b$", new int[] {} }, //
		});
	}

	@ParameterizedTest
	@MethodSource
	void findPath(String spath, String squery, int groups[]) throws ParseException
	{
		IPath<KVValue, KVLabel>        path      = KVPaths.pathFromString(spath);
		IPath<KVValue, KVLabel>        query     = KVPaths.pathFromString(squery);
		IFPA<KVValue, KVLabel>         automaton = automatonFromPath(path);
		ITreeMatcher<KVValue, KVLabel> matcher   = automaton.matcher(query);
		int                            rooted    = BooleanUtils.toInteger(path.isRooted());
		int                            i         = 0;

		while (matcher.find())
		{
			assertEquals(query.subPath(groups[i], groups[i] + path.nbLabels() + rooted), matcher.toMatchResult().group());
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
	void pathRewriting(Collection<IRule<KVValue, KVLabel>> rules, int maxDepth, String regex, String query, boolean expected) throws ParseException
	{
		GCPathRuleApplierSimple<KVValue, KVLabel> modifier = new GCPathRuleApplierSimple<>(maxDepth);
		IGRD<KVValue, KVLabel>                    grd      = new GRDFactory<>(rules, new BetaDependencyValidation<>(KVPathUnifiers.get())).create();

		IFPA<KVValue, KVLabel> automaton = fpaFactory(KVPaths.pathFromString(regex)) //
			.setGraphChunkModifier(modifier.getGraphChunkModifier(grd)) //
			.create();

		assertEquals(expected, automaton.matcher(KVPaths.pathFromString(query)).matches());
	}
}
