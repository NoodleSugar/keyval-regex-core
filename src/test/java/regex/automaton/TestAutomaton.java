package regex.automaton;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
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

import insomnia.fsa.FSAException;
import insomnia.fsa.IFSAutomaton;
import insomnia.help.HelpLists;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPath;
import insomnia.implem.kv.regex.RegexParser;
import insomnia.implem.kv.regex.automaton.RegexAutomatonFactory;
import insomnia.implem.kv.regex.element.IElement;

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
						return new RegexAutomatonFactory<KVLabel>((IElement) obj);
					}

				} }, //
				{ "Sync", new Function()
				{
					@Override
					public Object apply(Object obj)
					{
						return new RegexAutomatonFactory<KVLabel>((IElement) obj).mustBeSync(true);
					}
				} }
		};
		return Stream.of(ret).collect(Collectors.toList());
	}

	static List<Object[]> mergeParameters(List<Object[]> a, List<Object[]> b)
	{
		List<Pair<Object[], Object[]>> ret    = HelpLists.product(a, b);
		List<Object[]>                 merged = HelpLists.mergePairsArrays(ret);
		return merged;
	}

	private static IFSAutomaton<KVLabel> parse(String regex, Function<IElement, RegexAutomatonFactory<KVLabel>> automatonFactoryProvider) throws IOException, ParseException, FSAException
	{
		IElement rparsed = new RegexParser().readRegexStream(IOUtils.toInputStream(regex, Charset.defaultCharset()));
		return automatonFactoryProvider.apply(rparsed).newBuild();
	}

	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	class subTests
	{
		IFSAutomaton<KVLabel> automaton;

		@BeforeAll
		void setup() throws IOException, ParseException, FSAException
		{
			String   regex   = "a*.b?.c+|(d.(e|f){2,5}).~r*e?g+~";
			IElement rparsed = new RegexParser().readRegexStream(IOUtils.toInputStream(regex, Charset.defaultCharset()));
			automaton = new RegexAutomatonFactory<KVLabel>(rparsed).mustBeSync(!true).newBuild();
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
			try
			{
				assertEquals(match, automaton.test(subject.getLabels()));
			}
			catch (FSAException e)
			{
				fail(e.getMessage());
			}
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
	void match(String fname, Function<IElement, RegexAutomatonFactory<KVLabel>> fprovider, String regex, KVPath subject, boolean match)
	{
		try
		{
			IFSAutomaton<KVLabel> automaton = parse(regex, fprovider);
			assertEquals(match, automaton.test(subject.getLabels()));
		}
		catch (IOException | ParseException | FSAException e)
		{
			fail(e.getMessage());
		}
	}
}
