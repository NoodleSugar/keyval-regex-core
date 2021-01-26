package insomnia.implem.kv.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.IPath;
import insomnia.data.PathOp;
import insomnia.lib.help.HelpLists;

class TestPaths
{
	public static IPath<KVValue, KVLabel> pathFromString(String p)
	{
		return KVPaths.pathFromString(p);
	}

	static List<Object[]> isSimplePrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("a      "), pathFromString("a          "), false }, //
				{ true, pathFromString("a      "), pathFromString("a.b        "), true }, //
				{ true, pathFromString("a      "), pathFromString("b.a        "), false }, //
				{ true, pathFromString("b.a    "), pathFromString("b.a        "), false }, //
				{ true, pathFromString("b.a.a  "), pathFromString("b.a        "), false }, //
				{ true, pathFromString("a.b.c.d"), pathFromString("a.b.c.d.z.z"), true }, //
				{ true, pathFromString("a.b    "), pathFromString("a          "), false }, //

				{ !true, pathFromString("a      "), pathFromString("a          "), true }, //
				{ !true, pathFromString("a      "), pathFromString("a.b        "), true }, //
				{ !true, pathFromString("a      "), pathFromString("b.a        "), false }, //
				{ !true, pathFromString("b.a    "), pathFromString("b.a        "), true }, //
				{ !true, pathFromString("b.a.a  "), pathFromString("b.a        "), false }, //
				{ !true, pathFromString("a.b.c.d"), pathFromString("a.b.c.d.z.z"), true }, //
				{ !true, pathFromString("a.b    "), pathFromString("a          "), false }, //

		});
	}

	@ParameterizedTest
	@MethodSource("isSimplePrefixSource")
	void isSimplePrefixTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, boolean isPrefix)
	{
		assertEquals(isPrefix, HelpLists.isPrefix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> isPrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString(" a"), pathFromString(" a"), false }, //
				{ true, pathFromString(" a"), pathFromString(".a"), false }, //
				{ true, pathFromString(".a"), pathFromString(" a"), false }, //
				{ true, pathFromString(".a"), pathFromString(".a"), false }, //
				// 4
				{ true, pathFromString(" a"), pathFromString(" a."), true }, //
				{ true, pathFromString(" a"), pathFromString(".a."), false }, //
				{ true, pathFromString(".a"), pathFromString(" a."), false }, //
				{ true, pathFromString(".a"), pathFromString(".a."), true }, //
				// 8
				{ true, pathFromString(" a"), pathFromString("a.b"), true }, //
				{ true, pathFromString(" a"), pathFromString(".a.b"), false }, //
				{ true, pathFromString(".a"), pathFromString(" a.b"), false }, //
				{ true, pathFromString(".a"), pathFromString(".a.b"), true }, //
				// 12
				{ true, pathFromString(" a."), pathFromString(" a.b"), false }, //
				{ true, pathFromString(" a."), pathFromString(".a.b"), false }, //
				{ true, pathFromString(".a."), pathFromString(" a.b"), false }, //
				{ true, pathFromString(".a."), pathFromString(".a.b"), false }, //
				// 16
				{ true, pathFromString(" a."), pathFromString(" a.b."), false }, //
				{ true, pathFromString(" a."), pathFromString(".a.b."), false }, //
				{ true, pathFromString(".a."), pathFromString(" a.b."), false }, //
				{ true, pathFromString(".a."), pathFromString(".a.b."), false }, //
				// 20

				// not proper

				{ !true, pathFromString(" a"), pathFromString(" a"), true }, //
				{ !true, pathFromString(" a"), pathFromString(".a"), false }, //
				{ !true, pathFromString(".a"), pathFromString(" a"), false }, //
				{ !true, pathFromString(".a"), pathFromString(".a"), true }, //
				// 24
				{ !true, pathFromString(" a"), pathFromString(" a."), true }, //
				{ !true, pathFromString(" a"), pathFromString(".a."), false }, //
				{ !true, pathFromString(".a"), pathFromString(" a."), false }, //
				{ !true, pathFromString(".a"), pathFromString(".a."), true }, //
				// 28
				{ !true, pathFromString(" a"), pathFromString("a.b"), true }, //
				{ !true, pathFromString(" a"), pathFromString(".a.b"), false }, //
				{ !true, pathFromString(".a"), pathFromString(" a.b"), false }, //
				{ !true, pathFromString(".a"), pathFromString(".a.b"), true }, //
				// 32
				{ !true, pathFromString(" a."), pathFromString(" a.b"), false }, //
				{ !true, pathFromString(" a."), pathFromString(".a.b"), false }, //
				{ !true, pathFromString(".a."), pathFromString(" a.b"), false }, //
				{ !true, pathFromString(".a."), pathFromString(".a.b"), false }, //
				// 36
				{ !true, pathFromString(" a."), pathFromString(" a.b."), false }, //
				{ !true, pathFromString(" a."), pathFromString(".a.b."), false }, //
				{ !true, pathFromString(".a."), pathFromString(" a.b."), false }, //
				{ !true, pathFromString(".a."), pathFromString(".a.b."), false }, //
				// 40
				{ !true, pathFromString(" a.b "), pathFromString(".a.b."), false }, //
				{ !true, pathFromString(" a.b."), pathFromString(".a.b."), false }, //
				{ !true, pathFromString(".a.b."), pathFromString(".a.b."), true }, //
				{ !true, pathFromString(".a.b "), pathFromString(".a.b."), true }, //
				// 44

				// Rooted/Terminal cases

				{ true, pathFromString(" a"), pathFromString("a.b"), true }, //
				{ true, pathFromString(" a"), pathFromString("a.b."), true }, //
				{ true, pathFromString(".a"), pathFromString(".a.b"), true }, //
				{ true, pathFromString(".a"), pathFromString(".a.b."), true }, //
				// 48
				{ !true, pathFromString(" a"), pathFromString("a.b"), true }, //
				{ !true, pathFromString(" a"), pathFromString("a.b."), true }, //
				{ !true, pathFromString(".a"), pathFromString(".a.b"), true }, //
				{ !true, pathFromString(".a"), pathFromString(".a.b."), true }, //
				// 52

		});
	}

	@ParameterizedTest
	@MethodSource("isPrefixSource")
	void isPrefixTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, boolean isPrefix)
	{
		assertEquals(isPrefix, PathOp.isPrefix(needle, haystack, proper));
	}

	static List<Object[]> isSimpleSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("a      "), pathFromString("a          "), false }, //
				{ true, pathFromString("a      "), pathFromString("a.b        "), false }, //
				{ true, pathFromString("a.b    "), pathFromString("a          "), false }, //
				{ true, pathFromString("a      "), pathFromString("b.a        "), true }, //
				{ true, pathFromString("b.a    "), pathFromString("b.a        "), false }, //
				{ true, pathFromString("b.a.a  "), pathFromString("b.a        "), false }, //
				{ true, pathFromString("a.b.c.d"), pathFromString("z.z.a.b.c.d"), true }, //

				{ !true, pathFromString("a      "), pathFromString("a          "), true }, //
				{ !true, pathFromString("a      "), pathFromString("a.b        "), false }, //
				{ !true, pathFromString("a.b    "), pathFromString("a          "), false }, //
				{ !true, pathFromString("a      "), pathFromString("b.a        "), true }, //
				{ !true, pathFromString("b.a    "), pathFromString("b.a        "), true }, //
				{ !true, pathFromString("b.a.a  "), pathFromString("b.a        "), false }, //
				{ !true, pathFromString("a.b.c.d"), pathFromString("z.z.a.b.c.d"), true }, //
		});
	}

	@ParameterizedTest
	@MethodSource("isSimpleSuffixSource")
	void isSimpleSuffixTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, boolean isSuffix)
	{
		assertEquals(isSuffix, HelpLists.isSuffix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> isSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("a "), pathFromString("a "), false }, //
				{ true, pathFromString("a "), pathFromString("a."), false }, //
				{ true, pathFromString("a."), pathFromString("a "), false }, //
				{ true, pathFromString("a."), pathFromString("a."), false }, //
				// 4
				{ true, pathFromString("a "), pathFromString(".a "), true }, //
				{ true, pathFromString("a "), pathFromString(".a."), false }, //
				{ true, pathFromString("a."), pathFromString(".a "), false }, //
				{ true, pathFromString("a."), pathFromString(".a."), true }, //
				// 8
				{ true, pathFromString("a "), pathFromString("b.a "), true }, //
				{ true, pathFromString("a "), pathFromString("b.a."), false }, //
				{ true, pathFromString("a."), pathFromString("b.a "), false }, //
				{ true, pathFromString("a."), pathFromString("b.a."), true }, //
				// 12
				{ true, pathFromString(".a "), pathFromString(" b.a "), false }, //
				{ true, pathFromString(".a "), pathFromString(" b.a."), false }, //
				{ true, pathFromString(".a."), pathFromString(" b.a "), false }, //
				{ true, pathFromString(".a."), pathFromString(" b.a."), false }, //
				// 16
				{ true, pathFromString(".a "), pathFromString(".b.a "), false }, //
				{ true, pathFromString(".a "), pathFromString(".b.a."), false }, //
				{ true, pathFromString(".a."), pathFromString(".b.a."), false }, //
				{ true, pathFromString(".a."), pathFromString(".b.a "), false }, //
				// 20
				{ true, pathFromString(" a.b "), pathFromString(".a.b."), false }, //
				{ true, pathFromString(" a.b."), pathFromString(".a.b."), true }, //
				{ true, pathFromString(".a.b."), pathFromString(".a.b."), false }, //
				{ true, pathFromString(".a.b "), pathFromString(".a.b."), false }, //
				// 24

				// not proper

				{ !true, pathFromString("a "), pathFromString("a "), true }, //
				{ !true, pathFromString("a "), pathFromString("a."), false }, //
				{ !true, pathFromString("a."), pathFromString("a "), false }, //
				{ !true, pathFromString("a."), pathFromString("a."), true }, //
				// 28
				{ !true, pathFromString("a "), pathFromString(".a "), true }, //
				{ !true, pathFromString("a "), pathFromString(".a."), false }, //
				{ !true, pathFromString("a."), pathFromString(".a "), false }, //
				{ !true, pathFromString("a."), pathFromString(".a."), true }, //
				// 32
				{ !true, pathFromString("a "), pathFromString("b.a "), true }, //
				{ !true, pathFromString("a "), pathFromString("b.a."), false }, //
				{ !true, pathFromString("a."), pathFromString("b.a "), false }, //
				{ !true, pathFromString("a."), pathFromString("b.a."), true }, //
				// 36
				{ !true, pathFromString(".a "), pathFromString(" b.a "), false }, //
				{ !true, pathFromString(".a "), pathFromString(" b.a."), false }, //
				{ !true, pathFromString(".a."), pathFromString(" b.a "), false }, //
				{ !true, pathFromString(".a."), pathFromString(" b.a."), false }, //
				// 40
				{ !true, pathFromString(".a "), pathFromString(".b.a "), false }, //
				{ !true, pathFromString(".a "), pathFromString(".b.a."), false }, //
				{ !true, pathFromString(".a."), pathFromString(".b.a."), false }, //
				{ !true, pathFromString(".a."), pathFromString(".b.a "), false }, //
				// 44
				{ !true, pathFromString(" a.b "), pathFromString(".a.b."), false }, //
				{ !true, pathFromString(" a.b."), pathFromString(".a.b."), true }, //
				{ !true, pathFromString(".a.b."), pathFromString(".a.b."), true }, //
				{ !true, pathFromString(".a.b "), pathFromString(".a.b."), false }, //
				// 48
		});
	}

	@ParameterizedTest
	@MethodSource("isSuffixSource")
	void isSuffixTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, boolean isSuffix)
	{
		assertEquals(isSuffix, PathOp.isSuffix(needle, haystack, proper));
	}

	static List<Object[]> findSimpleInclusionsSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("a    "), pathFromString("a            "), new int[] {} }, //
				{ true, pathFromString("a    "), pathFromString("a.b.a.a      "), new int[] { 2 } }, //
				{ true, pathFromString("a.b.a"), pathFromString("a.b.a.b.a    "), new int[] {} }, //
				{ true, pathFromString("a.b.a"), pathFromString("z.a.b.a.b.a.z"), new int[] { 1, 3 } }, //

				{ !true, pathFromString("a    "), pathFromString("a            "), new int[] {} }, //
				{ !true, pathFromString("a    "), pathFromString("a.b.a.a      "), new int[] { 0, 2, 3 } }, //
				{ !true, pathFromString("a.b.a"), pathFromString("a.b.a.b.a    "), new int[] { 0, 2 } }, //
				{ !true, pathFromString("a.b.a"), pathFromString("z.a.b.a.b.a.z"), new int[] { 1, 3 } }, //

				{ !true, pathFromString("a.b.c"), pathFromString("x.a.b.e.c.z"), new int[] {} }, //
				{ !true, pathFromString("a.b.c"), pathFromString("e.a.b.c.f  "), new int[] { 1 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findSimpleInclusionsSource")
	void findSimpleInclusionTest(boolean noSuffOrPref, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, int[] result)
	{
		assertArrayEquals(result, HelpLists.findInclusions(needle.getLabels(), haystack.getLabels(), noSuffOrPref));
	}

	static List<Object[]> findInclusionsSource()
	{
		return Arrays.asList(new Object[][] { //
				{ !true, pathFromString(" a "), pathFromString(" a "), new int[] {} }, //
				{ !true, pathFromString(" a "), pathFromString(".a "), new int[] { 1 } }, //
				{ !true, pathFromString(" a "), pathFromString(" a."), new int[] { 0 } }, //
				{ !true, pathFromString(" a "), pathFromString(".a."), new int[] { 1 } }, //
				// 4
				{ !true, pathFromString(" a."), pathFromString(" a "), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(".a "), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(" a."), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(".a."), new int[] { 1 } }, //
				// 8
				{ !true, pathFromString(".a "), pathFromString(" a "), new int[] {} }, //
				{ !true, pathFromString(".a "), pathFromString(".a "), new int[] {} }, //
				{ !true, pathFromString(".a "), pathFromString(" a."), new int[] {} }, //
				{ !true, pathFromString(".a "), pathFromString(".a."), new int[] { 0 } }, //
				// 12
				{ !true, pathFromString(".a."), pathFromString(" a "), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(".a "), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(" a."), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(".a."), new int[] {} }, //
				// 16
				{ !true, pathFromString(" a "), pathFromString(" a.b "), new int[] { 0 } }, //
				{ !true, pathFromString(" a "), pathFromString(".a.b "), new int[] { 1 } }, //
				{ !true, pathFromString(" a "), pathFromString(" a.b."), new int[] { 0 } }, //
				{ !true, pathFromString(" a "), pathFromString(".a.b."), new int[] { 1 } }, //
				// 20
				{ !true, pathFromString(" a."), pathFromString(" a.b "), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(".a.b "), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(" a.b."), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(".a.b."), new int[] {} }, //
				// 24
				{ !true, pathFromString(".a "), pathFromString(" a.b "), new int[] {} }, //
				{ !true, pathFromString(".a "), pathFromString(".a.b "), new int[] { 0 } }, //
				{ !true, pathFromString(".a "), pathFromString(" a.b."), new int[] {} }, //
				{ !true, pathFromString(".a "), pathFromString(".a.b."), new int[] { 0 } }, //
				// 28
				{ !true, pathFromString(".a."), pathFromString(" a.b "), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(".a.b "), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(" a.b."), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(".a.b."), new int[] {} }, //
				// 32
				{ true, pathFromString("y"), pathFromString(".a.y"), new int[] {} }, //
				{ true, pathFromString("y"), pathFromString("y.b."), new int[] {} }, //

				{ !true, pathFromString("y"), pathFromString(".a.y"), new int[] { 2 } }, //
				{ !true, pathFromString("y"), pathFromString("y.b."), new int[] { 0 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findInclusionsSource")
	void findInclusionTest(boolean noSuffOrPref, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, int[] result)
	{
		assertArrayEquals(result, PathOp.findInclusions(needle, haystack, noSuffOrPref));
	}

	static List<Object[]> findSimpleSuffixPrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("a.b      "), pathFromString("b.a      "), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a    "), pathFromString("a.b.a    "), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a.b  "), pathFromString("a.b.a.b.z"), new int[] { 2 } }, //
				{ true, pathFromString("a.b.a    "), pathFromString("a.b.a.z  "), new int[] { 1 } }, //
				{ true, pathFromString("z.a.b.a.b"), pathFromString("a.b.a.b  "), new int[] { 2 } }, //
				{ true, pathFromString("z.a.b.a  "), pathFromString("a.b.a    "), new int[] { 1 } }, //

				{ !true, pathFromString("a.b      "), pathFromString("b.a      "), new int[] { 1 } }, //
				{ !true, pathFromString("a.b.a    "), pathFromString("a.b.a    "), new int[] { 1, 3 } }, //
				{ !true, pathFromString("a.b.a.b  "), pathFromString("a.b.a.b.z"), new int[] { 2, 4 } }, //
				{ !true, pathFromString("a.b.a    "), pathFromString("a.b.a.z  "), new int[] { 1, 3 } }, //
				{ !true, pathFromString("z.a.b.a.b"), pathFromString("a.b.a.b  "), new int[] { 2, 4 } }, //
				{ !true, pathFromString("z.a.b.a  "), pathFromString("a.b.a    "), new int[] { 1, 3 } }, //

				{ true, pathFromString("a"), pathFromString("a"), new int[] {} }, //
				{ true, pathFromString("a.b"), pathFromString("b.a.b.a"), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a"), pathFromString("b.a.b.a"), new int[] { 2 } }, //
				{ true, pathFromString("a.a.a.b"), pathFromString("a.a.a.a"), new int[] {} }, //
				{ true, pathFromString("a.b.a.b"), pathFromString("b.a.b.a"), new int[] { 1, 3 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findSimpleSuffixPrefixSource")
	void findSuffixPrefixTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, int[] result)
	{
		assertArrayEquals(result, HelpLists.findSuffixPrefix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> findSimplePrefixSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("a.b      "), pathFromString("b.a      "), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a    "), pathFromString("a.b.a    "), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a.b  "), pathFromString("z.a.b.a.b"), new int[] { 2 } }, //
				{ true, pathFromString("a.b.a    "), pathFromString("z.a.b.a  "), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a.b.z"), pathFromString("a.b.a.b  "), new int[] { 2 } }, //
				{ true, pathFromString("a.b.a.z  "), pathFromString("a.b.a    "), new int[] { 1 } }, //

				{ !true, pathFromString("a.b      "), pathFromString("b.a      "), new int[] { 1 } }, //
				{ !true, pathFromString("a.b.a    "), pathFromString("a.b.a    "), new int[] { 1, 3 } }, //
				{ !true, pathFromString("a.b.a.b  "), pathFromString("z.a.b.a.b"), new int[] { 2, 4 } }, //
				{ !true, pathFromString("a.b.a    "), pathFromString("z.a.b.a  "), new int[] { 1, 3 } }, //
				{ !true, pathFromString("a.b.a.b.z"), pathFromString("a.b.a.b  "), new int[] { 2, 4 } }, //
				{ !true, pathFromString("a.b.a.z  "), pathFromString("a.b.a    "), new int[] { 1, 3 } }, //

				{ true, pathFromString("a"), pathFromString("a"), new int[] {} }, //
				{ true, pathFromString("a.b"), pathFromString("b.a.b.a"), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a"), pathFromString("b.a.b.a"), new int[] { 1 } }, //
				{ true, pathFromString("a.a.a.b"), pathFromString("a.a.a.a"), new int[] { 1, 2, 3 } }, //
				{ true, pathFromString("a.b.a.b"), pathFromString("b.a.b.a"), new int[] { 1, 3 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findSimplePrefixSuffixSource")
	void findSimplePrefixSuffixTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, int[] result)
	{
		assertArrayEquals(result, HelpLists.findPrefixSuffix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> findPossiblePrefixesSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("a.b  "), pathFromString("a.b.c"), new int[] { 2 } }, //
				{ true, pathFromString("a.b  "), pathFromString("b    "), new int[] { 1 } }, //
				{ true, pathFromString("a.b  "), pathFromString("a.b  "), new int[] {} }, //
				{ true, pathFromString("a.b.a"), pathFromString("a.b  "), new int[] { 1 } }, //
				{ true, pathFromString("a.b.a"), pathFromString("b.a  "), new int[] { 2 } }, //
				// 5
				{ !true, pathFromString("a.b  "), pathFromString("a.b.c"), new int[] { 2 } }, //
				{ !true, pathFromString("a.b  "), pathFromString("b    "), new int[] { 1 } }, //
				{ !true, pathFromString("a.b  "), pathFromString("a.b  "), new int[] { 2 } }, //
				{ !true, pathFromString("a.b.a"), pathFromString("a.b  "), new int[] { 1 } }, //
				{ !true, pathFromString("a.b.a"), pathFromString("b.a  "), new int[] { 2 } }, //
				// 10
				{ true, pathFromString(" a.b."), pathFromString("b "), new int[] {} }, //
				{ true, pathFromString(" a.b."), pathFromString("b."), new int[] { 2 } }, //
				{ true, pathFromString(".a.b."), pathFromString("b "), new int[] {} }, //
				{ true, pathFromString(".a.b."), pathFromString("b."), new int[] { 2 } }, //
				// 14
				{ !true, pathFromString(".a"), pathFromString(" a"), new int[] { 1 } }, //
				{ !true, pathFromString(" a"), pathFromString(".a"), new int[] {} }, //
				{ !true, pathFromString(".a"), pathFromString(".a"), new int[] { 2 } }, //
				// 17
				{ !true, pathFromString(".b.a"), pathFromString(" a"), new int[] { 1 } }, //
				{ !true, pathFromString(" b.a"), pathFromString(".a"), new int[] {} }, //
				{ !true, pathFromString(".b.a"), pathFromString(".a"), new int[] {} }, //
				// 20
				{ !true, pathFromString(".a"), pathFromString(" a.b"), new int[] { 1 } }, //
				{ !true, pathFromString(" a"), pathFromString(".a.b"), new int[] {} }, //
				{ !true, pathFromString(".a"), pathFromString(".a.b"), new int[] { 2 } }, //
				// 23

				// Impossible cases
				{ !true, pathFromString(" a."), pathFromString(" a "), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(".a "), new int[] {} }, //
				{ !true, pathFromString(" a "), pathFromString(".a "), new int[] {} }, //
				{ !true, pathFromString(" a."), pathFromString(".a "), new int[] {} }, //
				// 27
				{ !true, pathFromString(" a."), pathFromString(".a."), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(".a "), new int[] {} }, //
				{ !true, pathFromString(" a "), pathFromString(".a."), new int[] {} }, //
				// 30

				// Possible cases
				{ !true, pathFromString(" a "), pathFromString(" a "), new int[] { 1 } }, //
				{ !true, pathFromString(" a "), pathFromString(" a."), new int[] { 1 } }, //
				{ !true, pathFromString(".a "), pathFromString(" a."), new int[] { 1 } }, //
				{ !true, pathFromString(".a "), pathFromString(" a "), new int[] { 1 } }, //
				{ !true, pathFromString(".a."), pathFromString(".a."), new int[] { 3 } }, //
				// 35
				{ !true, pathFromString(".a "), pathFromString(".a "), new int[] { 2 } }, //
				{ !true, pathFromString(".a "), pathFromString(".a."), new int[] { 2 } }, //
				{ !true, pathFromString(".a."), pathFromString(" a."), new int[] { 2 } }, //
				{ !true, pathFromString(" a."), pathFromString(" a."), new int[] { 2 } }, //
				// 39

				{ true, pathFromString(" a "), pathFromString(" a "), new int[] {} }, //
				{ true, pathFromString(" a "), pathFromString(" a."), new int[] { 1 } }, //
				{ true, pathFromString(".a "), pathFromString(" a."), new int[] { 1 } }, //
				{ true, pathFromString(".a "), pathFromString(" a "), new int[] { 1 } }, //
				{ true, pathFromString(".a."), pathFromString(".a."), new int[] {} }, //
				// 44
				{ true, pathFromString(".a "), pathFromString(".a "), new int[] {} }, //
				{ true, pathFromString(".a "), pathFromString(".a."), new int[] { 2 } }, //
				{ true, pathFromString(".a."), pathFromString(" a."), new int[] { 2 } }, //
				{ true, pathFromString(" a."), pathFromString(" a."), new int[] {} }, //
				// 48
		});
	}

	@ParameterizedTest
	@MethodSource("findPossiblePrefixesSource")
	void findPossiblePrefixesTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, int[] result)
	{
		assertArrayEquals(result, PathOp.findPossiblePrefixes(needle, haystack, proper));
	}

	static List<Object[]> findPossibleSuffixesSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, pathFromString("b.c  "), pathFromString("a.b.c"), new int[] { 2 } }, //
				{ true, pathFromString("b.a  "), pathFromString("b    "), new int[] { 1 } }, //
				{ true, pathFromString("a.b  "), pathFromString("a.b  "), new int[] {} }, //
				{ true, pathFromString("b.a.b"), pathFromString("a.b  "), new int[] { 1 } }, //
				{ true, pathFromString("b.a.b"), pathFromString("b.a  "), new int[] { 2 } }, //
				// 5
				{ !true, pathFromString("b.c  "), pathFromString("a.b.c"), new int[] { 2 } }, //
				{ !true, pathFromString("b.a  "), pathFromString("b    "), new int[] { 1 } }, //
				{ !true, pathFromString("a.b  "), pathFromString("a.b  "), new int[] { 2 } }, //
				{ !true, pathFromString("b.a.b"), pathFromString("a.b  "), new int[] { 1 } }, //
				{ !true, pathFromString("b.a.b"), pathFromString("b.a  "), new int[] { 2 } }, //
				// 10

				// Impossible cases
				{ !true, pathFromString(" a "), pathFromString(" a."), new int[] {} }, //
				{ !true, pathFromString(" a "), pathFromString(".a."), new int[] {} }, //
				{ !true, pathFromString(".a "), pathFromString(" a "), new int[] {} }, //
				{ !true, pathFromString(".a "), pathFromString(" a."), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(" a "), new int[] {} }, //
				// 15

				{ !true, pathFromString(".a "), pathFromString(".a."), new int[] {} }, //
				{ !true, pathFromString(".a."), pathFromString(" a."), new int[] {} }, //
				// 17

				// Possible cases
				{ !true, pathFromString(" a "), pathFromString(" a "), new int[] { 1 } }, //
				{ !true, pathFromString(" a "), pathFromString(".a "), new int[] { 1 } }, //
				{ !true, pathFromString(" a."), pathFromString(" a "), new int[] { 1 } }, //
				// 20

				{ !true, pathFromString(" a."), pathFromString(".a "), new int[] { 1 } }, //
				{ !true, pathFromString(" a."), pathFromString(" a."), new int[] { 2 } }, //
				{ !true, pathFromString(" a."), pathFromString(".a."), new int[] { 2 } }, //
				{ !true, pathFromString(".a."), pathFromString(".a."), new int[] { 3 } }, //
				{ !true, pathFromString(".a "), pathFromString(".a "), new int[] { 2 } }, //
				{ !true, pathFromString(".a."), pathFromString(".a "), new int[] { 2 } }, //
				// 26

				{ true, pathFromString(" a "), pathFromString(" a "), new int[] {} }, //
				{ true, pathFromString(" a "), pathFromString(".a "), new int[] { 1 } }, //
				{ true, pathFromString(" a."), pathFromString(" a "), new int[] { 1 } }, //
				{ true, pathFromString(" a."), pathFromString(".a "), new int[] { 1 } }, //
				// 30

				{ true, pathFromString(" a."), pathFromString(" a."), new int[] {} }, //
				{ true, pathFromString(" a."), pathFromString(".a."), new int[] { 2 } }, //
				{ true, pathFromString(".a."), pathFromString(".a."), new int[] {} }, //
				{ true, pathFromString(".a "), pathFromString(".a "), new int[] {} }, //
				{ true, pathFromString(".a."), pathFromString(".a "), new int[] { 2 } }, //
				// 35
		});
	}

	@ParameterizedTest
	@MethodSource("findPossibleSuffixesSource")
	void findPossibleSuffixesTest(boolean proper, IPath<KVValue, KVLabel> needle, IPath<KVValue, KVLabel> haystack, int[] result)
	{
		assertArrayEquals(result, PathOp.findPossibleSuffixes(needle, haystack, proper));
	}
}
