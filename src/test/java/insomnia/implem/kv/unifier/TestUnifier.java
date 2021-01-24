package insomnia.implem.kv.unifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		return KVPaths.pathFromString(p);
	}

	public static PathUnifier<KVValue, KVLabel> unifierFromStrings(String pb, String sb, String ph, String sh)
	{
		return unifierFromStrings(pb, sb, ph, sh, "");
	}

	public static PathUnifier<KVValue, KVLabel> unifierFromStrings(String pb, String sb, String ph, String sh, String ref)
	{
		return new PathUnifier<>(pathFromString(pb), pathFromString(sb), pathFromString(ph), pathFromString(sh), pathFromString(ref));
	}

	static List<Object[]> computeSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("b.a.b.a"), pathFromString("a.b"), new PathUnifier[] { //
						unifierFromStrings("a", "", "", "a.b.a", "b"), //
						unifierFromStrings("", "b", "b.a.b", "", "a"), //
						unifierFromStrings("", "", "b", "a", "a.b"), //
				} }, //
				{ pathFromString("a.a.a"), pathFromString("a.a"), new PathUnifier[] { //
						unifierFromStrings("a", "", "", "a.a", "a"), //
						unifierFromStrings("", "", "", "a", "a.a"), //
						unifierFromStrings("", "", "a", "", "a.a"), //
						unifierFromStrings("", "a", "a.a", "", "a"), //
				} }, //
		});

	}

	@ParameterizedTest
	@MethodSource("computeSource")
	void compute(IPath<KVValue, KVLabel> head, IPath<KVValue, KVLabel> body, PathUnifier<KVValue, KVLabel> ref_unifiers[])
	{
		Collection<IPathUnifier<KVValue, KVLabel>> unifiers = UNIFIERS.compute(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (PathUnifier<KVValue, KVLabel> ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> weakKVPathUnifiersSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("A.y"), pathFromString("y.B"), new PathUnifier[] { //
						unifierFromStrings("", "B", "A", "", "y"), //
				} }, //
				{ pathFromString("y.A"), pathFromString("B.y"), new PathUnifier[] { //
						unifierFromStrings("B", "", "", "A", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("y.B"), new PathUnifier[] { //
						unifierFromStrings("", "B", "", "", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("B.y"), new PathUnifier[] { //
						unifierFromStrings("B", "", "", "", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("B.y.B"), new PathUnifier[] { //
						unifierFromStrings("B", "B", "", "", "y"), //
				} }, //

				// Strong cases
				{ pathFromString("A.y  "), pathFromString("y"), new PathUnifier[] {} }, //
				{ pathFromString("  y.A"), pathFromString("y"), new PathUnifier[] {} }, //
				{ pathFromString("A.y.A"), pathFromString("y"), new PathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("y"), new PathUnifier[] {} }, //

				// Fixed cases
				{ pathFromString(".A.y"), pathFromString("y.B"), new PathUnifier[] { //
						unifierFromStrings("", "B", ".A", "", "y"), //
				} }, //
				{ pathFromString("A.y"), pathFromString("y.B."), new PathUnifier[] { //
						unifierFromStrings("", "B.", "A", "", "y"), //
				} }, //
				{ pathFromString(".A.y"), pathFromString("y.B."), new PathUnifier[] { //
						unifierFromStrings("", "B.", ".A", "", "y"), //
				} }, //

				// False fixed cases
				{ pathFromString(" A.y."), pathFromString(" y.B"), new PathUnifier[] {} }, //
				{ pathFromString(" A.y "), pathFromString(".y.B"), new PathUnifier[] {} }, //
				{ pathFromString(".A.y."), pathFromString(".y.B"), new PathUnifier[] {} }, //

		});
	}

	@ParameterizedTest
	@MethodSource("weakKVPathUnifiersSource")
	void weakKVPathUnifier(IPath<KVValue, KVLabel> head, IPath<KVValue, KVLabel> body, PathUnifier<KVValue, KVLabel> ref_unifiers[])
	{
		Collection<IPathUnifier<KVValue, KVLabel>> unifiers = UNIFIERS.weakUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (PathUnifier<KVValue, KVLabel> ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> strongKVPathUnifiersSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("A.y"), pathFromString("y"), new PathUnifier[] { //
						unifierFromStrings("", "", "A", "", "y"), //
				} }, //
				{ pathFromString("y.A"), pathFromString("y"), new PathUnifier[] { //
						unifierFromStrings("", "", "", "A", "y"), //
				} }, //
				{ pathFromString("A.y.A"), pathFromString("y"), new PathUnifier[] { //
						unifierFromStrings("", "", "A", "A", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("y"), new PathUnifier[] { //
						unifierFromStrings("", "", "", "", "y"), //
				} }, //
				// Weak cases
				{ pathFromString(" A.y  "), pathFromString("y.B"), new PathUnifier[] {} }, //
				{ pathFromString(".A.y  "), pathFromString("y."), new PathUnifier[] {} }, //
				{ pathFromString(" A.y  "), pathFromString("y."), new PathUnifier[] {} }, //
				{ pathFromString("  y.A"), pathFromString("B.y  "), new PathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("  y.B"), new PathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("B.y  "), new PathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("B.y.B"), new PathUnifier[] {} }, //

				// Fixed cases
				{ pathFromString(".A.y"), pathFromString("y"), new PathUnifier[] { //
						unifierFromStrings("", "", ".A", "", "y"), //
				} }, //
				// False fixed cases
				{ pathFromString("A.y"), pathFromString(".y"), new PathUnifier[] {} }, //
		});
	}

	@ParameterizedTest
	@MethodSource("strongKVPathUnifiersSource")
	void strongKVPathUnifier(IPath<KVValue, KVLabel> head, IPath<KVValue, KVLabel> body, PathUnifier<KVValue, KVLabel> ref_unifiers[])
	{
		Collection<IPathUnifier<KVValue, KVLabel>> unifiers = UNIFIERS.strongUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (PathUnifier<KVValue, KVLabel> ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}
}
