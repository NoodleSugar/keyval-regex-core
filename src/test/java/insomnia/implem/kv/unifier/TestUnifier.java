package insomnia.implem.kv.unifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.data.IPath;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPaths;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.unifier.PathUnifier;
import insomnia.unifier.IPathUnifier;
import insomnia.unifier.PathUnifiers;

class TestUnifier
{
	PathUnifiers<KVValue, KVLabel> UNIFIERS = KVPathUnifiers.get();

	public static IPath<KVValue, KVLabel> pathFromString(String p)
	{
		try
		{
			return KVPaths.pathFromString(p);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}

	public static PathUnifier<KVValue, KVLabel> unifierFromStrings(String pb, String sb, String ph, String sh)
	{
		return unifierFromStrings(pb, sb, ph, sh, "");
	}

	public static PathUnifier<KVValue, KVLabel> unifierFromStrings(String pb, String sb, String ph, String sh, String ref)
	{
		return new PathUnifier<>(pathFromString(pb), pathFromString(sb), pathFromString(ph), pathFromString(sh), pathFromString(ref));
	}

	static List<Object[]> compute()
	{
		return Arrays.asList(new Object[][] { //
				{ "b.a.b.a", "a.b", new PathUnifier[] { //
						unifierFromStrings("a", "", "", "a.b.a", "b"), //
						unifierFromStrings("", "b", "b.a.b", "", "a"), //
						unifierFromStrings("", "", "b", "a", "a.b"), //
				} }, //
				{ "a.a.a", "a.a", new PathUnifier[] { //
						unifierFromStrings("a", "", "", "a.a", "a"), //
						unifierFromStrings("", "", "", "a", "a.a"), //
						unifierFromStrings("", "", "a", "", "a.a"), //
						unifierFromStrings("", "a", "a.a", "", "a"), //
				} }, //
		});

	}

	@ParameterizedTest
	@MethodSource
	void compute(String shead, String sbody, PathUnifier<KVValue, KVLabel> ref_unifiers[])
	{
		IPath<KVValue, KVLabel>                    head     = pathFromString(shead);
		IPath<KVValue, KVLabel>                    body     = pathFromString(sbody);
		Collection<IPathUnifier<KVValue, KVLabel>> unifiers = UNIFIERS.compute(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (PathUnifier<KVValue, KVLabel> ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> weakKVPathUnifier()
	{
		return Arrays.asList(new Object[][] { //
				{ "A.y", "y.B", new PathUnifier[] { //
						unifierFromStrings("", "B", "A", "", "y"), //
				} }, //
				{ "y.A", "B.y", new PathUnifier[] { //
						unifierFromStrings("B", "", "", "A", "y"), //
				} }, //
				{ "y", "y.B", new PathUnifier[] { //
						unifierFromStrings("", "B", "", "", "y"), //
				} }, //
				{ "y", "B.y", new PathUnifier[] { //
						unifierFromStrings("B", "", "", "", "y"), //
				} }, //
				{ "y", "B.y.B", new PathUnifier[] { //
						unifierFromStrings("B", "B", "", "", "y"), //
				} }, //

				// Strong cases
				{ "A.y  ", "y", new PathUnifier[] {} }, //
				{ "  y.A", "y", new PathUnifier[] {} }, //
				{ "A.y.A", "y", new PathUnifier[] {} }, //
				{ "  y  ", "y", new PathUnifier[] {} }, //

				// Fixed cases
				{ "^.A.y", "y.B", new PathUnifier[] { //
						unifierFromStrings("", "B", "^.A", "", "y"), //
				} }, //
				{ "A.y", "y.B$", new PathUnifier[] { //
						unifierFromStrings("", "B$", "A", "", "y"), //
				} }, //
				{ "^.A.y", "y.B$", new PathUnifier[] { //
						unifierFromStrings("", "B$", "^.A", "", "y"), //
				} }, //

				// False fixed cases
				{ " A.y$", " y.B", new PathUnifier[] {} }, //
				{ " A.y ", "^.y.B", new PathUnifier[] {} }, //
				{ "^.A.y$", "^.y.B", new PathUnifier[] {} }, //

		});
	}

	@ParameterizedTest
	@MethodSource
	void weakKVPathUnifier(String shead, String sbody, PathUnifier<KVValue, KVLabel> ref_unifiers[])
	{
		IPath<KVValue, KVLabel>                    head     = pathFromString(shead);
		IPath<KVValue, KVLabel>                    body     = pathFromString(sbody);
		Collection<IPathUnifier<KVValue, KVLabel>> unifiers = UNIFIERS.weakUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (PathUnifier<KVValue, KVLabel> ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> strongKVPathUnifier()
	{
		return Arrays.asList(new Object[][] { //
				{ "A.y", "y", new PathUnifier[] { //
						unifierFromStrings("", "", "A", "", "y"), //
				} }, //
				{ "y.A", "y", new PathUnifier[] { //
						unifierFromStrings("", "", "", "A", "y"), //
				} }, //
				{ "A.y.A", "y", new PathUnifier[] { //
						unifierFromStrings("", "", "A", "A", "y"), //
				} }, //
				{ "y", "y", new PathUnifier[] { //
						unifierFromStrings("", "", "", "", "y"), //
				} }, //
				// Weak cases
				{ " A.y  ", "y.B", new PathUnifier[] {} }, //
				{ "^.A.y  ", "y$", new PathUnifier[] {} }, //
				{ " A.y  ", "y$", new PathUnifier[] {} }, //
				{ "  y.A", "B.y  ", new PathUnifier[] {} }, //
				{ "  y  ", "  y.B", new PathUnifier[] {} }, //
				{ "  y  ", "B.y  ", new PathUnifier[] {} }, //
				{ "  y  ", "B.y.B", new PathUnifier[] {} }, //

				// Fixed cases
				{ "^.A.y", "y", new PathUnifier[] { //
						unifierFromStrings("", "", "^.A", "", "y"), //
				} }, //
				// False fixed cases
				{ "A.y", "^.y", new PathUnifier[] {} }, //
		});
	}

	@ParameterizedTest
	@MethodSource
	void strongKVPathUnifier(String shead, String sbody, PathUnifier<KVValue, KVLabel> ref_unifiers[])
	{
		IPath<KVValue, KVLabel>                    head     = pathFromString(shead);
		IPath<KVValue, KVLabel>                    body     = pathFromString(sbody);
		Collection<IPathUnifier<KVValue, KVLabel>> unifiers = UNIFIERS.strongUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (PathUnifier<KVValue, KVLabel> ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}
}
