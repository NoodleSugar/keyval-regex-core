package regex.automaton;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.ITree;
import insomnia.fsa.FSAException;
import insomnia.fsa.IFSAutomaton;
import insomnia.help.HelpLists;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPath;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.fsa.KVGraphChunkPathRuleApplierSimple;
import insomnia.implem.kv.pregex.IPRegexElement;
import insomnia.implem.kv.pregex.PRegexParser;
import insomnia.implem.kv.pregex.fsa.PRegexFSAFactory;
import insomnia.implem.kv.rule.KVPathRule;
import insomnia.rule.IRule;

public class TestAutomaton
{
	static List<Object[]> factories()
	{
		@SuppressWarnings("rawtypes")
		Object ret[][] = { //
				{ "General", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return new PRegexFSAFactory<KVValue, KVLabel>((IPRegexElement) obj);
					}

				} }, //
				{ "Sync", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return new PRegexFSAFactory<KVValue, KVLabel>((IPRegexElement) obj).mustBeSync(true);
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

	private static IFSAutomaton<ITree<KVValue, KVLabel>> parse(String regex, Function<IPRegexElement, PRegexFSAFactory<KVValue, KVLabel>> automatonFactoryProvider) throws IOException, ParseException, FSAException
	{
		return parse(regex, automatonFactoryProvider, null);
	}

	private static IFSAutomaton<ITree<KVValue, KVLabel>> parse(String regex, Function<IPRegexElement, PRegexFSAFactory<KVValue, KVLabel>> automatonFactoryProvider, KVValue value) throws IOException, ParseException, FSAException
	{
		IPRegexElement rparsed = new PRegexParser().parse(IOUtils.toInputStream(regex, Charset.defaultCharset()), value);
		return automatonFactoryProvider.apply(rparsed).newBuild();
	}

	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	class subTests
	{
		IFSAutomaton<ITree<KVValue, KVLabel>> automaton;

		@BeforeAll
		void setup() throws IOException, ParseException, FSAException
		{
			String   regex   = "a*.b?.c+|(d.(e|f){2,5}).~r*e?g+~";
			IPRegexElement rparsed = new PRegexParser().readRegexStream(IOUtils.toInputStream(regex, Charset.defaultCharset()));
			automaton = new PRegexFSAFactory<KVValue, KVLabel>(rparsed).mustBeSync(!true).newBuild();
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
		void complex(KVPath subject, boolean match)
		{
			assertEquals(match, automaton.test(subject));
		}
	}

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
		});
		return mergeParameters(factories(), a);
	}

	@ParameterizedTest
	@MethodSource
	void match(String fname, Function<IPRegexElement, PRegexFSAFactory<KVValue, KVLabel>> fprovider, String regex, KVPath subject, boolean match)
	{
		try
		{
			IFSAutomaton<ITree<KVValue, KVLabel>> automaton = parse(regex, fprovider);
			assertEquals(match, automaton.test(subject));
		}
		catch (IOException | ParseException | FSAException e)
		{
			fail(e.getMessage());
		}
	}

	static List<Object[]> matchValue()
	{
		List<Object[]> a = Arrays.asList(new Object[][] { //
				{ "a.b", new KVValue(15), KVPath.pathFromString("a.b", new KVValue(15)), true }, //
				{ "a.b", new KVValue(15), KVPath.pathFromString("a.b", new KVValue(16)), false }, //
				{ "a.b", new KVValue(15), KVPath.pathFromString("a.b"), false }, //
		});
		return mergeParameters(factories(), a);
	}

	@ParameterizedTest
	@MethodSource
	void matchValue(String fname, Function<IPRegexElement, PRegexFSAFactory<KVValue, KVLabel>> fprovider, String regex, KVValue rvalue, KVPath subject, boolean match)
	{
		try
		{
			IFSAutomaton<ITree<KVValue, KVLabel>> automaton = parse(regex, fprovider, rvalue);
			assertEquals(match, automaton.test(subject));
		}
		catch (IOException | ParseException | FSAException e)
		{
			fail(e.getMessage());
		}
	}

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
				return Arrays.asList(KVPathRule.create("x", "c"));
			}

			@Override
			public List<Object[]> getTestObjects()
			{
				return Arrays.asList(new Object[][] { //
						{ "a.b.c", new Object[][] { //
								{ "a.b.c", true }, //
								{ "a.b.x", true }, //
								{ "a.b", false }, //
								{ "a.b.c.c", false }, //
								{ "a.b.d", false }, //
						}, //
						} });
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
				for (Object[] expected : (Object[][]) dataObjects[1])
				{
					List<Object> items = new ArrayList<>();
					items.add(data.getRules());

					// The query
					items.add(dataObjects[0]);

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
	void pathRewriting(Collection<IRule<KVValue, KVLabel>> rules, String regex, String query, boolean expected) throws IOException, ParseException, FSAException
	{
		IPRegexElement                    rparsed  = new PRegexParser().parse(IOUtils.toInputStream(regex, Charset.defaultCharset()));
		KVGraphChunkPathRuleApplierSimple modifier = new KVGraphChunkPathRuleApplierSimple(2);

		IFSAutomaton<ITree<KVValue, KVLabel>> automaton = new PRegexFSAFactory<KVValue, KVLabel>(rparsed) //
			.setGraphChunkModifier(modifier.getGraphChunkModifier(rules)) //
			.newBuild();

		assertEquals(expected, automaton.test(KVPath.pathFromString(query)));
	}
}
