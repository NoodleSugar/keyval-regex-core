package insomnia.implem.fsa.fpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.IPath;
import insomnia.data.ITree;
import insomnia.data.regex.IPathMatcher;
import insomnia.fsa.fpa.IFPA;
import insomnia.implem.data.Paths;
import insomnia.implem.data.Trees;
import insomnia.implem.data.regex.parser.IPRegexElement;
import insomnia.implem.data.regex.parser.PRegexParser;
import insomnia.implem.fsa.fpa.creational.FPAFactory;
import insomnia.implem.fsa.fpa.graphchunk.modifier.GCPathRuleApplierSimple;
import insomnia.implem.rule.PathRules;
import insomnia.implem.rule.dependency.BetaDependencyValidation;
import insomnia.implem.rule.grd.creational.GRDFactory;
import insomnia.implem.unifier.PathUnifier;
import insomnia.lib.help.HelpLists;
import insomnia.rule.IRule;
import insomnia.rule.grd.IGRD;
import insomnia.unifier.PathUnifiers;

public class TestAutomaton
{
	static FPAFactory<String, String> fpaFactory(IPRegexElement e)
	{
		return new FPAFactory<>(e, s -> s, s -> s);
	}

	static FPAFactory<String, String> fpaFactory(IPath<String, String> path)
	{
		return new FPAFactory<>(path, s -> s, s -> s);
	}

	static IPath<String, String> pathFromString(String path)
	{
		PRegexParser parser = new PRegexParser(Collections.emptyMap());
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

	private static IFPA<String, String> parse(String regex, Function<IPRegexElement, IFPA<String, String>> automatonFactoryProvider) throws IOException, ParseException
	{
		IPRegexElement rparsed = parser.parse(IOUtils.toInputStream(regex, Charset.defaultCharset()));
		return automatonFactoryProvider.apply(rparsed);
	}

	// =========================================================================

	public static Object[][][] searchForIn_extended()
	{
		return new Object[][][] { //
				{ { "^.x.a+|y.b+" }, //
						{ "^.x.b", 0 }, //
						{ "^.x.a.b", 1 }, //
						{ "^.y.b{1,10}", 1 }, //
						{ "^.y.a", 0 }, //
				}, //
				{ { "^a?$" }, //
						{ "^$", 1 }, //
						{ "^a$", 1 }, //
						{ "^a", 0 }, //
						{ "a$", 0 }, //
						{ "^a.a$", 0 }, //
						{ "a", 0 }, //
						{ "", 0 }, //
						{ "^", 0 }, //
						{ "$", 0 }, //
				}, //
				{ { "^a*.b|^x" }, { "^a.x", 0 } }, //
				{ { "a*.b?.c*.c$|(^d.(e|f){2,5}).~r*e?g+~$" }, //
						{ "a.d", 0 }, //
						{ "a.b.c", 0 }, //
						{ "(^)?.a{0,2}.b?.c{1,2}", 0 }, //
						{ "(^)?.a{0,2}.b?.c{1,2}$", 1 }, //

						{ "(^d.e{5}).reg$", 1 }, //
						{ "(^d.e{6}).reg$", 0 }, //

						{ "(^d.(e|f){2}).(reg|rrgg|g|egg)$", 1 }, //

						{ "a.b.b.c$", 1 }, //
						{ "a.b", 0 }, //
						{ "d.e.reg", 0 }, //
						{ "e.e.reg", 0 }, //
						{ "d.f.f.re", 0 }, //
						{ "c.e", 0 }, //
						{ "a.b.c.e$", 0 }, //
				}, //
		};
	}

	public static Object[][][] searchForIn()
	{
		// SearchFor -> { searchIn }
		return new Object[][][] { //
				// ====================================================================
				// Empty
				{ { "" }, //
						{ "(=99)?", 1 }, //
						{ "a.(=99)?", 2 }, //
				}, //
				{ { "^" }, //
						{ "X?.($)?", 0 }, //
						{ "^.(=99)?.X?.($)?", 1 }, //
				}, //
				{ { "$" }, //
						{ "(^)?.X?", 0 }, //
						{ "(^)?.X?.($).(=99)", 1 }, //
				}, //
				{ { "^$" }, //
						{ "^$", 1 }, //
						{ "^.X?.(=99)?", 0 }, //
						{ "X?.(=99)?.$", 0 }, //
						{ "X?.(=99)?", 0 }, //
				}, //
				// ====================================================================
				// Value
				{ { "=99" }, //
						{ "(^)?.=99.(X=0)?.($)?", 1 }, //
						{ "(^)?.=99.(_)?.X=99.($)?", 2 }, //
						{ "(^)?.(=0)?.($)?", 0 }, //
				}, //
				{ { "^=99" }, //
						{ "^=99.(X=0)?.($)?", 1 }, //
						{ "^=99.(_)?.X=99.($)?", 1 }, //
						{ "^.(=0)?.($)?", 0 }, //
				}, //
				{ { "=99$" }, //
						{ "(^)?.X?.=99$", 1 }, //
						{ "(^)?.=99.(_)?.X=99$", 1 }, //
						{ "(^)?.(=0)?.X?.$", 0 }, //
				}, //
				{ { "^=99$" }, //
						{ "^=99$", 1 }, //
						{ "^=99.(X=99){1,2}.($)?", 0 }, //
						{ "(^)?.(X=99){1,2}$", 0 }, //
				}, //
				// ====================================================================
				// Key
				{ { "a" }, //
						{ "(^)?.X?.a.(=99)?.X?.($)?", 1 }, //
						{ "(^)?.X?.a.(=99)?.X?.a.(=99)?.($)?", 2 }, //
						{ "(^)?.X?.($)?", 0 }, //
						{ "X(X?.a,X.b)", 1 }, //
				}, //
				{ { "a=99" }, //
						{ "(^)?.X?.a=99.X?.($)?", 1 }, //
						{ "(^)?.X?.a=99.X?.a.(=0)?.($)?", 1 }, //
						{ "(^)?.X?.a=99.X?.a=99.($)?", 2 }, //
						{ "(^)?.X=99?.($)?", 0 }, //
				}, //
				{ { "^a" }, //
						{ "^a.(=99)?.X?.($)?", 1 }, //
						{ "^a.(=99)?.X?.a.(=99)?.($)?", 1 }, //
						{ "(^)?.X?.($)?", 0 }, //
						{ "X(X.a,X.b)", 0 }, //
						{ "a(X.a,X.b)", 0 }, //
				}, //
				{ { "a$" }, //
						{ "(^)?.X?.a.(=99)?.$", 1 }, //
						{ "(^)?.a.(=99)?.X?.a.(=99)?.$", 1 }, //
						{ "(^)?.X?.($)?", 0 }, //
						{ "X(X?.a$,X.b)", 1 }, //
				}, //
				// ====================================================================
				// Quantifier
				{ { "a{3}" }, //
						{ "a.a.a", 1 }, //
						{ "a.a.a.a", 2 }, //
						{ "a.a.a.a.a", 3 }, //
						{ "a{0,2}.(X.a)?", 0 }, //
				}, //
				{ { "a{1,2}" }, //
						{ "a", 1 }, //
						{ "a.a", 3 }, //
						{ "a.a.a", 5 }, //
						{ "a.a.a.a", 7 }, //
				}, //
				{ { "a?" }, //
						{ "", 1 }, //
						{ "a", 3 }, //
						{ "a.a", 5 }, //
						{ "a.a.a", 7 }, //
						{ "a.a.a.a", 9 }, //
				}, //
				{ { "a*" }, //
						{ "", 1 }, //
						{ "a", 3 }, //
						{ "a.a", 6 }, //
						{ "a.a.a", 10 }, //
						{ "a.a.a.a", 15 }, //
						{ "a" + StringUtils.repeat(".a", 10), 78 }, //
				}, //
				{ { "^a*" }, //
						{ "^", 1 }, //
						{ "^a", 2 }, //
						{ "^a.a", 6 }, //
						{ "X?.a{0,3}", 0 }, //
				}, //
				{ { "a+" }, //
						{ "", 0 }, //
						{ "a", 1 }, //
						{ "a.a", 3 }, //
						{ "a.a.a", 6 }, //
						{ "a" + StringUtils.repeat(".a", 10), 66 }, //
				}, //
				// ====================================================================
				// Sequence
				{ { "a.b" }, //
						{ "(^)?.X?.a.b.(=99)?.X?.($)?", 1 }, //
						{ "(^)?.X?.a.b.X?.a.b.X?.($)?", 2 }, //
						{ "(^)?.X?.a.X.b.X?.($)?", 0 }, //
				}, //
				{ { "^a.b" }, //
						{ "^a.b.X?.(a.b)?.($)?", 1 }, //
						{ "a.b.X?.(a.b)?.($)?", 0 }, //
				}, //
				{ { "a.b$" }, //
						{ "(^)?.(a.b)?.X?.a.b$", 1 }, //
						{ "(^)?.(a.b)?.X?.a.b", 0 }, //
				}, //
				{ { "^a.b$" }, //
						{ "^a.b$", 1 }, //
						{ "((^)?.X)?.a.b.(X.($)?)?", 0 }, //
				}, //
				{ { "a=99.b" }, //
						{ "a=99.b.a.b", 1 }, //
						{ "(a=99.b){3}", 3 }, //
						{ "(a.b=99){3}", 0 }, //
				}, //
				{ { "^a=99.b" }, //
						{ "(^a=99.b){3}", 1 }, //
						{ "(a=99.b){3}", 0 }, //
				}, //
				{ { "a.b=99" }, //
						{ "a.b=99.a.b", 1 }, //
						{ "(a.b=99){3}", 3 }, //
						{ "(a=99.b){3}", 0 }, //
				}, //
				{ { "a.b=99$" }, //
						{ "(a.b=99){3}$", 1 }, //
						{ "(a.b=99){3}", 0 }, //
				}, //
					 // ====================================================================
					 // Disjunction
				{ { "a|b" }, //
						{ "(^)?.X?.(a|b).X?.($)?", 1 }, //
						{ "(^)?.X?.($)?", 0 }, //
				}, //
				{ { "a=88|b=99" }, //
						{ "(^)?.X?.(a|b).X?.($)?", 0 }, //
						{ "(^)?.X?.(a=88|b=99).X?.($)?", 1 }, //
				}, //
				{ { "(^a=88)|(b=99$)" }, //
						{ "^a=88.X?.b=99$", 2 }, //
						{ "a=88.X?.b=99$", 1 }, //
						{ "^a=88.X?.b=99", 1 }, //
						{ "a=88.X?.b=99", 0 }, //
						{ "^a.X?.b=99$", 1 }, //
						{ "^a=88.X?.b$", 1 }, //
						{ "^a.X?.b$", 0 }, //
				}, //
				// ====================================================================
				// Compound
				{ { "a|b.c" }, //
						{ "(^)?.X?.(a|b.c).X?.($)?", 1 }, //
						{ "(^)?.X?.(b|c).X?.($)?", 0 }, //
				}, //
				{ { "(a|b).c" }, //
						{ "(^)?.X?.((a|b).c).X?.($)?", 1 }, //
						{ "(^)?.X?.(a|b|c).X?.($)?", 0 }, //
				}, //
				// ====================================================================
				// Trees
				{ { "a(b,c)" }, //
						{ "(^)?.X?.a.(=1)?.(b.(=1)?.(X)?.($)?,c.(=1)?.X?.($)?)", 1 }, //
						{ "a(b|c,X)", 0 }, //
				}, //
				{ { "^a(b,c)" }, //
						{ "^a(b,c)", 1 }, //
						{ "((^)?.X)?.a(b,c)", 0 }, //
				}, //
				{ { "a(b,c$)" }, //
						{ "(^)?.X?.a(b.X?.($)?,c.(=1)?.$)", 1 }, //
						{ "a(b,c)", 0 }, //
						{ "a(X,c$)", 0 }, //
				}, //
				{ { "^a(aa,ab=5$),b$" }, //
						{ "^a(aa.(=0)?.X,ab=5$),b$", 1 }, //
						{ "^a(aa.(=0)?.X?.($)?,ab=5$),b$", 1 }, //
						{ "^a(aa.X?,ab$),b$", 0 }, //
						{ "^a(aa.X?,ab=6$),b$", 0 }, //
						{ ".a(aa.X?,ab=5$),b$", 0 }, //
						{ "^a(aa.X?,ab=5$),b.X?", 0 }, //
				}, //
		};
	}

	public static Object[][][] searchInFor()
	{
		// SearchIn -> SearchFor
		return new Object[][][] { //
				{ { "X.a(b.X,c.X)" }, //
						{ "a", 1 }, //
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
				{ { "^=1.(b=2,c=1(a,b=2,x=1))" }, //
						{ "=1", 3 }, //
						{ "=2", 2 }, //
						{ "=3", 0 }, //
						{ "^=1", 1 }, //
						{ "b=2", 2 }, //
						{ "^b=2", 1 }, //
						{ "b=1", 0 }, //
				}, //
		};
	}

	// ==========================================================================

	public static Object[][][] allSearchForIn()
	{
		return ArrayUtils.addAll( //
			ArrayUtils.addAll(searchForIn(), searchForIn_extended() //
			), searchInFor_reverse(searchInFor()) //
		);
	}

	private static Stream<Object[]> searchForIn(Object[][][] arrays) throws ParseException
	{
		return Arrays.stream(ArrayUtils.addAll(arrays)) //
			.flatMap((item) -> {
				List<Object[]>   ret       = new ArrayList<>();
				Iterator<Object[]> it      = IteratorUtils.arrayIterator(item);
				String           searchFor = (String) it.next()[0];
				try
				{
					if (!Trees.getParser().parse(searchFor).isPath())
						return Stream.empty();

					while (it.hasNext())
					{
						Object[]                  subItem = it.next();
						List<IPath<String, String>> paths;

						IPRegexElement e = Trees.getParser().parse((String) subItem[0]);
						if (!e.isPath())
							continue;

						paths = Paths.pathsFromPRegexElement(e);

						for (IPath<?, ?> path : paths)
							ret.add(new Object[] { searchFor, path, subItem[1] });
					}
					return mergeParameters(factories(), ret).stream();
				}
				catch (ParseException e)
				{
					throw new AssertionError(e);
				}
			});
	}

	private static Object[][][] searchInFor_reverse(Object[][][] searchInFor)
	{
		Map<String, List<Object[]>> datas = new HashMap<>();

		Iterator<Object[][]> itemIterator = IteratorUtils.arrayIterator(searchInFor);

		while (itemIterator.hasNext())
		{
			Iterator<Object[]> subItemIterator = IteratorUtils.arrayIterator(itemIterator.next());

			String searchIn = (String) subItemIterator.next()[0];

			while (subItemIterator.hasNext())
			{
				Object[]       subItem   = subItemIterator.next();
				String         searchFor = (String) subItem[0];
				List<Object[]> objs      = datas.computeIfAbsent(searchFor, (k) -> new ArrayList<>(Collections.singleton(new Object[] { searchFor })));
				Object[]       newItem   = new Object[] { searchIn, subItem[1] };
				objs.add(newItem);
			}
		}
		Object ret[][][] = new Object[datas.size()][][];
		int    i         = 0;
		for (String k : datas.keySet())
		{
			List<Object[]> data = datas.get(k);
			ret[i++] = data.toArray(new Object[0][]);
		}
		return ret;
	}

	// ==========================================================================

	static Stream<Object[]> arguments() throws ParseException
	{
		return searchForIn(allSearchForIn());
	}

	@ParameterizedTest
	@MethodSource("arguments")
	void match(String fname, Function<IPRegexElement, IFPA<String, String>> fprovider, String searchFor, IPath<String, String> psearchIn, int nb) throws IOException, ParseException
	{
		boolean match = nb > 0;

		IFPA<String, String> automaton = parse(searchFor, fprovider);
		assertEquals(match, automaton.matcher(psearchIn).matches());
	}

	// ==========================================================================

	@ParameterizedTest
	@MethodSource("arguments")
	void find(String fname, Function<IPRegexElement, IFPA<String, String>> fprovider, String searchFor, IPath<String, String> psearchIn, int nb) throws ParseException, IOException
	{
		List<IPath<String, String>> psearchFor = null;
		try
		{
			psearchFor = Paths.pathsFromString(searchFor);
		}
		catch (Exception e)
		{
		}
		IFPA<String, String>         automaton = parse(searchFor, fprovider);
		IPathMatcher<String, String> matcher   = automaton.matcher(psearchIn);

		int i = 0;
		while (matcher.find())
		{
			IPath<String, String> group = matcher.toMatchResult().group();

			if (null != psearchFor)
				assertTrue(psearchFor.stream().anyMatch(sFor -> ITree.projectEquals(sFor, group)), //
					String.format("Expected \n%s; but have\n%s", psearchFor, ITree.toString(group)));

			assertTrue(ITree.isSubTreeOf(group, psearchIn), //
				String.format("Expected\n%s to be a subtree of\n %s", ITree.toString(group), ITree.toString(psearchIn)));
			i++;
		}
		if (nb == 0)
			assertEquals(0, i);
		else// TODO: equality if possible
			assertTrue(nb <= i, String.format("Expected %d <= %d", nb, i));
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
