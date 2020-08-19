package rule;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.rule.tree.Path;
import insomnia.rule.tree.Paths;

class TestPaths
{

	public static Path pathFromString(String p)
	{
		return pathFromString(p, false, false);
	}

	public static Path pathFromString(String p, boolean isRooted, boolean isTerminal)
	{
		return new Path(isRooted, isTerminal, p.split(Pattern.quote(".")));
	}

	static List<Object[]> isSimplePrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("a"), pathFromString("a"), false }, //
				{ pathFromString("a"), pathFromString("a.b"), true }, //
				{ pathFromString("a"), pathFromString("b.a"), false }, //
				{ pathFromString("b.a"), pathFromString("b.a"), false }, //
				{ pathFromString("b.a.a"), pathFromString("b.a"), false }, //
				{ pathFromString("a.b.c.d"), pathFromString("a.b.c.d.z.z"), true }, //
		});
	}

	@ParameterizedTest
	@MethodSource("isSimplePrefixSource")
	void isSimplePrefixTest(Path needle, Path haystack, boolean isPrefix)
	{
		assertEquals(isPrefix, Paths.isSimplePrefix(needle, haystack));
	}

	static List<Object[]> isPrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ //
						pathFromString("a", true, false), //
						pathFromString("a.b", false, false), //
						true //
				}, //
				{ //
						pathFromString("a", true, false), //
						pathFromString("a.b", true, false), //
						true //
				}, //
				{ //
						pathFromString("a", false, false), //
						pathFromString("a.b", true, false), //
						false //
				}, //
				{ //
						pathFromString("a", true, true), //
						pathFromString("a.b", false, false), //
						false //
				}, //
				{ //
						pathFromString("a", true, true), //
						pathFromString("a.b", true, false), //
						false //
				}, //
		});
	}

	@ParameterizedTest
	@MethodSource("isPrefixSource")
	void isPrefixTest(Path needle, Path haystack, boolean isPrefix)
	{
		assertEquals(isPrefix, Paths.isPrefix(needle, haystack));
	}

	static List<Object[]> isSimpleSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("a"), pathFromString("a"), true }, //
				{ pathFromString("a"), pathFromString("a.b"), false }, //
				{ pathFromString("a.b"), pathFromString("a"), false }, //
				{ pathFromString("a"), pathFromString("b.a"), true }, //
				{ pathFromString("b.a"), pathFromString("b.a"), true }, //
				{ pathFromString("b.a.a"), pathFromString("b.a"), false }, //
				{ pathFromString("a.b.c.d"), pathFromString("z.z.a.b.c.d"), true }, //
		});
	}

	@ParameterizedTest
	@MethodSource("isSimpleSuffixSource")
	void isSimpleSuffixTest(Path needle, Path haystack, boolean isSuffix)
	{
		assertEquals(isSuffix, Paths.isSimpleSuffix(needle, haystack));
	}

	static List<Object[]> findAllSimpleInclusionsSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("a"), pathFromString("a"), new int[] {} }, //
				{ pathFromString("a"), pathFromString("a.b.a.a"), new int[] { 2 } }, //
				{ pathFromString("a.b.a"), pathFromString("a.b.a.b.a"), new int[] { } }, //
				{ pathFromString("a.b.a"), pathFromString("z.a.b.a.b.a.z"), new int[] { 1, 3 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findAllSimpleInclusionsSource")
	void findAllSimpleInclusionTest(Path needle, Path haystack, int[] result)
	{
		assertArrayEquals(result, Paths.findAllSimpleInclusions(needle, haystack));
	}

	static List<Object[]> findAllSimpleSuffixPrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("a.b"), pathFromString("b.a"), new int[] { 1 } }, //
				{ pathFromString("a.b.a"), pathFromString("a.b.a"), new int[] { 1 } }, //
				{ pathFromString("a.b.a.b"), pathFromString("a.b.a.b.z"), new int[] { 2, 4 } }, //
				{ pathFromString("a.b.a"), pathFromString("a.b.a.z"), new int[] { 1, 3 } }, //
				{ pathFromString("z.a.b.a.b"), pathFromString("a.b.a.b"), new int[] { 2, 4 } }, //
				{ pathFromString("z.a.b.a"), pathFromString("a.b.a"), new int[] { 1, 3 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findAllSimpleSuffixPrefixSource")
	void findAllSuffixPrefixTest(Path needle, Path haystack, int[] result)
	{
		assertArrayEquals(result, Paths.findAllSimpleSuffixPrefix(needle, haystack));
	}

	static List<Object[]> findAllSimplePrefixSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("a.b"), pathFromString("b.a"), new int[] { 1 } }, //
				{ pathFromString("a.b.a"), pathFromString("a.b.a"), new int[] { 1 } }, //
				{ pathFromString("a.b.a.b"), pathFromString("z.a.b.a.b"), new int[] { 2, 4 } }, //
				{ pathFromString("a.b.a"), pathFromString("z.a.b.a"), new int[] { 1, 3 } }, //
				{ pathFromString("a.b.a.b.z"), pathFromString("a.b.a.b"), new int[] { 2, 4 } }, //
				{ pathFromString("a.b.a.z"), pathFromString("a.b.a"), new int[] { 1, 3 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findAllSimplePrefixSuffixSource")
	void findAllPrefixSuffixTest(Path needle, Path haystack, int[] result)
	{
		assertArrayEquals(result, Paths.findAllSimplePrefixSuffix(needle, haystack));
	}

	static List<Object[]> findAllPossiblePrefixesSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("a.b"), pathFromString("a.b.c"), new int[] { 2 } }, //
				{ pathFromString("a.b"), pathFromString("b"), new int[] { 1 } }, //
				{ pathFromString("a.b"), pathFromString("a.b"), new int[] {} }, //
				{ pathFromString("a.b.a"), pathFromString("a.b"), new int[] { 1 } }, //
				{ pathFromString("a.b.a"), pathFromString("b.a"), new int[] { 2 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findAllPossiblePrefixesSource")
	void findAllPossiblePrefixesTest(Path needle, Path haystack, int[] result)
	{
		assertArrayEquals(result, Paths.findAllPossiblePrefixes(needle, haystack));
	}
}
