package insomnia.implem.kv.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
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
		try
		{
			return KVPaths.pathFromString(p.trim());
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	static List<Object[]> isSimplePrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "a      ", "a          ", false }, //
				{ true, "a      ", "a.b        ", true }, //
				{ true, "a      ", "b.a        ", false }, //
				{ true, "b.a    ", "b.a        ", false }, //
				{ true, "b.a.a  ", "b.a        ", false }, //
				{ true, "a.b.c.d", "a.b.c.d.z.z", true }, //
				{ true, "a.b    ", "a          ", false }, //

				{ !true, "a      ", "a          ", true }, //
				{ !true, "a      ", "a.b        ", true }, //
				{ !true, "a      ", "b.a        ", false }, //
				{ !true, "b.a    ", "b.a        ", true }, //
				{ !true, "b.a.a  ", "b.a        ", false }, //
				{ !true, "a.b.c.d", "a.b.c.d.z.z", true }, //
				{ !true, "a.b    ", "a          ", false }, //

		});
	}

	@ParameterizedTest
	@MethodSource("isSimplePrefixSource")
	void isSimplePrefixTest(boolean proper, String sneedle, String shaystack, boolean isPrefix)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertEquals(isPrefix, HelpLists.isPrefix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> isPrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "  a", "  a", false }, //
				{ true, "  a", "^.a", false }, //
				{ true, "^.a", "  a", false }, //
				{ true, "^.a", "^.a", false }, //
				// 4
				{ true, "  a", "  a$", true }, //
				{ true, "  a", "^.a$", false }, //
				{ true, "^.a", "  a$", false }, //
				{ true, "^.a", "^.a$", true }, //
				// 8
				{ true, "  a", "  a.b", true }, //
				{ true, "  a", "^.a.b", false }, //
				{ true, "^.a", "  a.b", false }, //
				{ true, "^.a", "^.a.b", true }, //
				// 12
				{ true, "  a$", "  a.b", false }, //
				{ true, "  a$", "^.a.b", false }, //
				{ true, "^.a$", "  a.b", false }, //
				{ true, "^.a$", "^.a.b", false }, //
				// 16
				{ true, "  a$", "  a.b$", false }, //
				{ true, "  a$", "^.a.b$", false }, //
				{ true, "^.a$", "  a.b$", false }, //
				{ true, "^.a$", "^.a.b$", false }, //
				// 20

				// not proper

				{ !true, "  a", "  a", true }, //
				{ !true, "  a", "^.a", false }, //
				{ !true, "^.a", "  a", false }, //
				{ !true, "^.a", "^.a", true }, //
				// 24
				{ !true, "  a", "  a$", true }, //
				{ !true, "  a", "^.a$", false }, //
				{ !true, "^.a", "  a$", false }, //
				{ !true, "^.a", "^.a$", true }, //
				// 28
				{ !true, "  a", "  a.b", true }, //
				{ !true, "  a", "^.a.b", false }, //
				{ !true, "^.a", "  a.b", false }, //
				{ !true, "^.a", "^.a.b", true }, //
				// 32
				{ !true, "  a$", "  a.b", false }, //
				{ !true, "  a$", "^.a.b", false }, //
				{ !true, "^.a$", "  a.b", false }, //
				{ !true, "^.a$", "^.a.b", false }, //
				// 36
				{ !true, "  a$", "  a.b$", false }, //
				{ !true, "  a$", "^.a.b$", false }, //
				{ !true, "^.a$", "  a.b$", false }, //
				{ !true, "^.a$", "^.a.b$", false }, //
				// 40
				{ !true, "  a.b ", "^.a.b$", false }, //
				{ !true, "  a.b$", "^.a.b$", false }, //
				{ !true, "^.a.b$", "^.a.b$", true }, //
				{ !true, "^.a.b ", "^.a.b$", true }, //
				// 44

				// Rooted/Terminal cases

				{ true, "  a", "  a.b", true }, //
				{ true, "  a", "  a.b$", true }, //
				{ true, "^.a", "^.a.b", true }, //
				{ true, "^.a", "^.a.b$", true }, //
				// 48:
				{ !true, "  a", "  a.b ", true }, //
				{ !true, "  a", "  a.b$", true }, //
				{ !true, "^.a", "^.a.b ", true }, //
				{ !true, "^.a", "^.a.b$", true }, //
				// 52

		});
	}

	@ParameterizedTest
	@MethodSource("isPrefixSource")
	void isPrefixTest(boolean proper, String sneedle, String shaystack, boolean isPrefix)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertEquals(isPrefix, PathOp.isPrefix(needle, haystack, proper));
	}

	static List<Object[]> isSimpleSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "a      ", "a          ", false }, //
				{ true, "a      ", "a.b        ", false }, //
				{ true, "a.b    ", "a          ", false }, //
				{ true, "a      ", "b.a        ", true }, //
				{ true, "b.a    ", "b.a        ", false }, //
				{ true, "b.a.a  ", "b.a        ", false }, //
				{ true, "a.b.c.d", "z.z.a.b.c.d", true }, //

				{ !true, "a      ", "a          ", true }, //
				{ !true, "a      ", "a.b        ", false }, //
				{ !true, "a.b    ", "a          ", false }, //
				{ !true, "a      ", "b.a        ", true }, //
				{ !true, "b.a    ", "b.a        ", true }, //
				{ !true, "b.a.a  ", "b.a        ", false }, //
				{ !true, "a.b.c.d", "z.z.a.b.c.d", true }, //
		});
	}

	@ParameterizedTest
	@MethodSource("isSimpleSuffixSource")
	void isSimpleSuffixTest(boolean proper, String sneedle, String shaystack, boolean isSuffix)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertEquals(isSuffix, HelpLists.isSuffix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> isSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "a ", "a ", false }, //
				{ true, "a ", "a$", false }, //
				{ true, "a$", "a ", false }, //
				{ true, "a$", "a$", false }, //
				// 4
				{ true, "a ", "^.a ", true }, //
				{ true, "a ", "^.a$", false }, //
				{ true, "a$", "^.a ", false }, //
				{ true, "a$", "^.a$", true }, //
				// 8
				{ true, "a ", "b.a ", true }, //
				{ true, "a ", "b.a$", false }, //
				{ true, "a$", "b.a ", false }, //
				{ true, "a$", "b.a$", true }, //
				// 12
				{ true, "^.a ", " b.a ", false }, //
				{ true, "^.a ", " b.a$", false }, //
				{ true, "^.a$", " b.a ", false }, //
				{ true, "^.a$", " b.a$", false }, //
				// 16
				{ true, "^.a ", "^.b.a ", false }, //
				{ true, "^.a ", "^.b.a$", false }, //
				{ true, "^.a$", "^.b.a$", false }, //
				{ true, "^.a$", "^.b.a ", false }, //
				// 20
				{ true, "  a.b ", "^.a.b$", false }, //
				{ true, "  a.b$", "^.a.b$", true }, //
				{ true, "^.a.b$", "^.a.b$", false }, //
				{ true, "^.a.b ", "^.a.b$", false }, //
				// 24

				// not proper

				{ !true, "a ", "a ", true }, //
				{ !true, "a ", "a$", false }, //
				{ !true, "a$", "a ", false }, //
				{ !true, "a$", "a$", true }, //
				// 28
				{ !true, "a ", "^.a ", true }, //
				{ !true, "a ", "^.a$", false }, //
				{ !true, "a$", "^.a ", false }, //
				{ !true, "a$", "^.a$", true }, //
				// 32
				{ !true, "a ", "b.a ", true }, //
				{ !true, "a ", "b.a$", false }, //
				{ !true, "a$", "b.a ", false }, //
				{ !true, "a$", "b.a$", true }, //
				// 36
				{ !true, "^.a ", " b.a ", false }, //
				{ !true, "^.a ", " b.a$", false }, //
				{ !true, "^.a$", " b.a ", false }, //
				{ !true, "^.a$", " b.a$", false }, //
				// 40
				{ !true, "^.a ", "^.b.a ", false }, //
				{ !true, "^.a ", "^.b.a$", false }, //
				{ !true, "^.a$", "^.b.a$", false }, //
				{ !true, "^.a$", "^.b.a ", false }, //
				// 44
				{ !true, " a.b ", "^.a.b$", false }, //
				{ !true, " a.b$", "^.a.b$", true }, //
				{ !true, "^.a.b$", "^.a.b$", true }, //
				{ !true, "^.a.b ", "^.a.b$", false }, //
				// 48
		});
	}

	@ParameterizedTest
	@MethodSource("isSuffixSource")
	void isSuffixTest(boolean proper, String sneedle, String shaystack, boolean isSuffix)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertEquals(isSuffix, PathOp.isSuffix(needle, haystack, proper));
	}

	static List<Object[]> findSimpleInclusionsSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "a    ", "a            ", new int[] {} }, //
				{ true, "a    ", "a.b.a.a      ", new int[] { 2 } }, //
				{ true, "a.b.a", "a.b.a.b.a    ", new int[] {} }, //
				{ true, "a.b.a", "z.a.b.a.b.a.z", new int[] { 1, 3 } }, //

				{ !true, "a    ", "a            ", new int[] {} }, //
				{ !true, "a    ", "a.b.a.a      ", new int[] { 0, 2, 3 } }, //
				{ !true, "a.b.a", "a.b.a.b.a    ", new int[] { 0, 2 } }, //
				{ !true, "a.b.a", "z.a.b.a.b.a.z", new int[] { 1, 3 } }, //

				{ !true, "a.b.c", "x.a.b.e.c.z", new int[] {} }, //
				{ !true, "a.b.c", "e.a.b.c.f  ", new int[] { 1 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findSimpleInclusionsSource")
	void findSimpleInclusionTest(boolean noSuffOrPref, String sneedle, String shaystack, int[] result)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertArrayEquals(result, HelpLists.findInclusions(needle.getLabels(), haystack.getLabels(), noSuffOrPref));
	}

	static List<Object[]> findInclusionsSource()
	{
		return Arrays.asList(new Object[][] { //
				{ !true, " a ", "  a ", new int[] {} }, //
				{ !true, " a ", "^.a ", new int[] { 1 } }, //
				{ !true, " a ", "  a$", new int[] { 0 } }, //
				{ !true, " a ", "^.a$", new int[] { 1 } }, //
				// 4
				{ !true, " a$", "  a ", new int[] {} }, //
				{ !true, " a$", "^.a ", new int[] {} }, //
				{ !true, " a$", "  a$", new int[] {} }, //
				{ !true, " a$", "^.a$", new int[] { 1 } }, //
				// 8
				{ !true, "^.a ", "  a ", new int[] {} }, //
				{ !true, "^.a ", "^.a ", new int[] {} }, //
				{ !true, "^.a ", "  a$", new int[] {} }, //
				{ !true, "^.a ", "^.a$", new int[] { 0 } }, //
				// 12
				{ !true, "^.a$", "  a ", new int[] {} }, //
				{ !true, "^.a$", "^.a ", new int[] {} }, //
				{ !true, "^.a$", "  a$", new int[] {} }, //
				{ !true, "^.a$", "^.a$", new int[] {} }, //
				// 16
				{ !true, " a ", "  a.b ", new int[] { 0 } }, //
				{ !true, " a ", "^.a.b ", new int[] { 1 } }, //
				{ !true, " a ", "  a.b$", new int[] { 0 } }, //
				{ !true, " a ", "^.a.b$", new int[] { 1 } }, //
				// 20
				{ !true, " a$", "  a.b ", new int[] {} }, //
				{ !true, " a$", "^.a.b ", new int[] {} }, //
				{ !true, " a$", "  a.b$", new int[] {} }, //
				{ !true, " a$", "^.a.b$", new int[] {} }, //
				// 24
				{ !true, "^.a ", "  a.b ", new int[] {} }, //
				{ !true, "^.a ", "^.a.b ", new int[] { 0 } }, //
				{ !true, "^.a ", "  a.b$", new int[] {} }, //
				{ !true, "^.a ", "^.a.b$", new int[] { 0 } }, //
				// 28
				{ !true, "^.a$", "  a.b ", new int[] {} }, //
				{ !true, "^.a$", "^.a.b ", new int[] {} }, //
				{ !true, "^.a$", "  a.b$", new int[] {} }, //
				{ !true, "^.a$", "^.a.b$", new int[] {} }, //
				// 32
				{ true, "y", "^.a.y", new int[] {} }, //
				{ true, "y", "y.b$", new int[] {} }, //

				{ !true, "y", "^.a.y", new int[] { 2 } }, //
				{ !true, "y", "y.b$", new int[] { 0 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findInclusionsSource")
	void findInclusionTest(boolean noSuffOrPref, String sneedle, String shaystack, int[] result)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertArrayEquals(result, PathOp.findInclusions(needle, haystack, noSuffOrPref));
	}

	static List<Object[]> findSimpleSuffixPrefixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "a.b      ", "b.a      ", new int[] { 1 } }, //
				{ true, "a.b.a    ", "a.b.a    ", new int[] { 1 } }, //
				{ true, "a.b.a.b  ", "a.b.a.b.z", new int[] { 2 } }, //
				{ true, "a.b.a    ", "a.b.a.z  ", new int[] { 1 } }, //
				{ true, "z.a.b.a.b", "a.b.a.b  ", new int[] { 2 } }, //
				{ true, "z.a.b.a  ", "a.b.a    ", new int[] { 1 } }, //

				{ !true, "a.b      ", "b.a      ", new int[] { 1 } }, //
				{ !true, "a.b.a    ", "a.b.a    ", new int[] { 1, 3 } }, //
				{ !true, "a.b.a.b  ", "a.b.a.b.z", new int[] { 2, 4 } }, //
				{ !true, "a.b.a    ", "a.b.a.z  ", new int[] { 1, 3 } }, //
				{ !true, "z.a.b.a.b", "a.b.a.b  ", new int[] { 2, 4 } }, //
				{ !true, "z.a.b.a  ", "a.b.a    ", new int[] { 1, 3 } }, //

				{ true, "a      ", "a      ", new int[] {} }, //
				{ true, "a.b    ", "b.a.b.a", new int[] { 1 } }, //
				{ true, "a.b.a  ", "b.a.b.a", new int[] { 2 } }, //
				{ true, "a.a.a.b", "a.a.a.a", new int[] {} }, //
				{ true, "a.b.a.b", "b.a.b.a", new int[] { 1, 3 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findSimpleSuffixPrefixSource")
	void findSuffixPrefixTest(boolean proper, String sneedle, String shaystack, int[] result)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertArrayEquals(result, HelpLists.findSuffixPrefix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> findSimplePrefixSuffixSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "a.b      ", "b.a      ", new int[] { 1 } }, //
				{ true, "a.b.a    ", "a.b.a    ", new int[] { 1 } }, //
				{ true, "a.b.a.b  ", "z.a.b.a.b", new int[] { 2 } }, //
				{ true, "a.b.a    ", "z.a.b.a  ", new int[] { 1 } }, //
				{ true, "a.b.a.b.z", "a.b.a.b  ", new int[] { 2 } }, //
				{ true, "a.b.a.z  ", "a.b.a    ", new int[] { 1 } }, //

				{ !true, "a.b      ", "b.a      ", new int[] { 1 } }, //
				{ !true, "a.b.a    ", "a.b.a    ", new int[] { 1, 3 } }, //
				{ !true, "a.b.a.b  ", "z.a.b.a.b", new int[] { 2, 4 } }, //
				{ !true, "a.b.a    ", "z.a.b.a  ", new int[] { 1, 3 } }, //
				{ !true, "a.b.a.b.z", "a.b.a.b  ", new int[] { 2, 4 } }, //
				{ !true, "a.b.a.z  ", "a.b.a    ", new int[] { 1, 3 } }, //

				{ true, "a      ", "a      ", new int[] {} }, //
				{ true, "a.b    ", "b.a.b.a", new int[] { 1 } }, //
				{ true, "a.b.a  ", "b.a.b.a", new int[] { 1 } }, //
				{ true, "a.a.a.b", "a.a.a.a", new int[] { 1, 2, 3 } }, //
				{ true, "a.b.a.b", "b.a.b.a", new int[] { 1, 3 } }, //
		});
	}

	@ParameterizedTest
	@MethodSource("findSimplePrefixSuffixSource")
	void findSimplePrefixSuffixTest(boolean proper, String sneedle, String shaystack, int[] result)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertArrayEquals(result, HelpLists.findPrefixSuffix(needle.getLabels(), haystack.getLabels(), proper));
	}

	static List<Object[]> findPossiblePrefixesSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "a.b  ", "a.b.c", new int[] { 2 } }, //
				{ true, "a.b  ", "b    ", new int[] { 1 } }, //
				{ true, "a.b  ", "a.b  ", new int[] {} }, //
				{ true, "a.b.a", "a.b  ", new int[] { 1 } }, //
				{ true, "a.b.a", "b.a  ", new int[] { 2 } }, //
				// 5
				{ !true, "a.b  ", "a.b.c", new int[] { 2 } }, //
				{ !true, "a.b  ", "b    ", new int[] { 1 } }, //
				{ !true, "a.b  ", "a.b  ", new int[] { 2 } }, //
				{ !true, "a.b.a", "a.b  ", new int[] { 1 } }, //
				{ !true, "a.b.a", "b.a  ", new int[] { 2 } }, //
				// 10
				{ true, "  a.b$", "b ", new int[] {} }, //
				{ true, "  a.b$", "b$", new int[] { 2 } }, //
				{ true, "^.a.b$", "b ", new int[] {} }, //
				{ true, "^.a.b$", "b$", new int[] { 2 } }, //
				// 14
				{ !true, "^.a", "  a", new int[] { 1 } }, //
				{ !true, "  a", "^.a", new int[] {} }, //
				{ !true, "^.a", "^.a", new int[] { 2 } }, //
				// 17
				{ !true, "^.b.a", "  a", new int[] { 1 } }, //
				{ !true, "  b.a", "^.a", new int[] {} }, //
				{ !true, "^.b.a", "^.a", new int[] {} }, //
				// 20
				{ !true, "^.a", "  a.b", new int[] { 1 } }, //
				{ !true, "  a", "^.a.b", new int[] {} }, //
				{ !true, "^.a", "^.a.b", new int[] { 2 } }, //
				// 23

				// Impossible cases
				{ !true, " a$", "  a ", new int[] {} }, //
				{ !true, " a$", "^.a ", new int[] {} }, //
				{ !true, " a ", "^.a ", new int[] {} }, //
				{ !true, " a$", "^.a ", new int[] {} }, //
				// 27
				{ !true, "  a$", "^.a$", new int[] {} }, //
				{ !true, "^.a$", "^.a ", new int[] {} }, //
				{ !true, "  a ", "^.a$", new int[] {} }, //
				// 30

				// Possible cases
				{ !true, "  a ", "  a ", new int[] { 1 } }, //
				{ !true, "  a ", "  a$", new int[] { 1 } }, //
				{ !true, "^.a ", "  a$", new int[] { 1 } }, //
				{ !true, "^.a ", "  a ", new int[] { 1 } }, //
				{ !true, "^.a$", "^.a$", new int[] { 3 } }, //
				// 35
				{ !true, "^.a ", "^.a ", new int[] { 2 } }, //
				{ !true, "^.a ", "^.a$", new int[] { 2 } }, //
				{ !true, "^.a$", "  a$", new int[] { 2 } }, //
				{ !true, "  a$", "  a$", new int[] { 2 } }, //
				// 39

				{ true, "  a ", "  a ", new int[] {} }, //
				{ true, "  a ", "  a$", new int[] { 1 } }, //
				{ true, "^.a ", "  a$", new int[] { 1 } }, //
				{ true, "^.a ", "  a ", new int[] { 1 } }, //
				{ true, "^.a$", "^.a$", new int[] {} }, //
				// 44
				{ true, "^.a ", "^.a ", new int[] {} }, //
				{ true, "^.a ", "^.a$", new int[] { 2 } }, //
				{ true, "^.a$", "  a$", new int[] { 2 } }, //
				{ true, "  a$", "  a$", new int[] {} }, //
				// 48
		});
	}

	@ParameterizedTest
	@MethodSource("findPossiblePrefixesSource")
	void findPossiblePrefixesTest(boolean proper, String sneedle, String shaystack, int[] result)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertArrayEquals(result, PathOp.findPossiblePrefixes(needle, haystack, proper));
	}

	static List<Object[]> findPossibleSuffixesSource()
	{
		return Arrays.asList(new Object[][] { //
				{ true, "b.c  ", "a.b.c", new int[] { 2 } }, //
				{ true, "b.a  ", "b    ", new int[] { 1 } }, //
				{ true, "a.b  ", "a.b  ", new int[] {} }, //
				{ true, "b.a.b", "a.b  ", new int[] { 1 } }, //
				{ true, "b.a.b", "b.a  ", new int[] { 2 } }, //
				// 5
				{ !true, "b.c  ", "a.b.c", new int[] { 2 } }, //
				{ !true, "b.a  ", "b    ", new int[] { 1 } }, //
				{ !true, "a.b  ", "a.b  ", new int[] { 2 } }, //
				{ !true, "b.a.b", "a.b  ", new int[] { 1 } }, //
				{ !true, "b.a.b", "b.a  ", new int[] { 2 } }, //
				// 10

				// Impossible cases
				{ !true, "  a ", "  a$", new int[] {} }, //
				{ !true, "  a ", "^.a$", new int[] {} }, //
				{ !true, "^.a ", "  a ", new int[] {} }, //
				{ !true, "^.a ", "  a$", new int[] {} }, //
				{ !true, "^.a$", "  a ", new int[] {} }, //
				// 15

				{ !true, "^.a ", "^.a$", new int[] {} }, //
				{ !true, "^.a$", "  a$", new int[] {} }, //
				// 17

				// Possible cases
				{ !true, " a ", " a ", new int[] { 1 } }, //
				{ !true, " a ", "^.a ", new int[] { 1 } }, //
				{ !true, " a$", " a ", new int[] { 1 } }, //
				// 20

				{ !true, "  a$", "^.a ", new int[] { 1 } }, //
				{ !true, "  a$", "  a$", new int[] { 2 } }, //
				{ !true, "  a$", "^.a$", new int[] { 2 } }, //
				{ !true, "^.a$", "^.a$", new int[] { 3 } }, //
				{ !true, "^.a ", "^.a ", new int[] { 2 } }, //
				{ !true, "^.a$", "^.a ", new int[] { 2 } }, //
				// 26

				{ true, " a ", "  a ", new int[] {} }, //
				{ true, " a ", "^.a ", new int[] { 1 } }, //
				{ true, " a$", "  a ", new int[] { 1 } }, //
				{ true, " a$", "^.a ", new int[] { 1 } }, //
				// 30

				{ true, "  a$", "  a$", new int[] {} }, //
				{ true, "  a$", "^.a$", new int[] { 2 } }, //
				{ true, "^.a$", "^.a$", new int[] {} }, //
				{ true, "^.a ", "^.a ", new int[] {} }, //
				{ true, "^.a$", "^.a ", new int[] { 2 } }, //
				// 35
		});
	}

	@ParameterizedTest
	@MethodSource("findPossibleSuffixesSource")
	void findPossibleSuffixesTest(boolean proper, String sneedle, String shaystack, int[] result)
	{
		IPath<KVValue, KVLabel> needle   = pathFromString(sneedle);
		IPath<KVValue, KVLabel> haystack = pathFromString(shaystack);
		assertArrayEquals(result, PathOp.findPossibleSuffixes(needle, haystack, proper));
	}
}
