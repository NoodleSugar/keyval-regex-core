package unifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPath;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.unifier.KVPathUnifier;
import insomnia.implem.kv.unifier.KVPathUnifiers;
import insomnia.unifier.PathUnifiers;

class TestUnifier
{
	PathUnifiers<KVValue, KVLabel, ? extends KVPathUnifier> UNIFIERS = KVPathUnifiers.get();

	public static KVPath pathFromString(String p)
	{
		return KVPath.pathFromString(p);
	}

	public static KVPathUnifier unifierFromStrings(String pb, String sb, String ph, String sh)
	{
		return unifierFromStrings(pb, sb, ph, sh, "");
	}

	public static KVPathUnifier unifierFromStrings(String pb, String sb, String ph, String sh, String ref)
	{
		return new KVPathUnifier(pathFromString(pb), pathFromString(sb), pathFromString(ph), pathFromString(sh), pathFromString(ref));
	}

	static List<Object[]> computeSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("b.a.b.a"), pathFromString("a.b"), new KVPathUnifier[] { //
						unifierFromStrings("a", "", "", "a.b.a", "b"), //
						unifierFromStrings("", "b", "b.a.b", "", "a"), //
						unifierFromStrings("", "", "b", "a", "a.b"), //
				} }, //
				{ pathFromString("a.a.a"), pathFromString("a.a"), new KVPathUnifier[] { //
						unifierFromStrings("a", "", "", "a.a", "a"), //
						unifierFromStrings("", "", "", "a", "a.a"), //
						unifierFromStrings("", "", "a", "", "a.a"), //
						unifierFromStrings("", "a", "a.a", "", "a"), //
				} }, //
		});

	}

	@ParameterizedTest
	@MethodSource("computeSource")
	void compute(KVPath head, KVPath body, KVPathUnifier ref_unifiers[])
	{
		List<? extends KVPathUnifier> unifiers = UNIFIERS.compute(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (KVPathUnifier ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> weakKVPathUnifiersSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("A.y"), pathFromString("y.B"), new KVPathUnifier[] { //
						unifierFromStrings("", "B", "A", "", "y"), //
				} }, //
				{ pathFromString("y.A"), pathFromString("B.y"), new KVPathUnifier[] { //
						unifierFromStrings("B", "", "", "A", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("y.B"), new KVPathUnifier[] { //
						unifierFromStrings("", "B", "", "", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("B.y"), new KVPathUnifier[] { //
						unifierFromStrings("B", "", "", "", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("B.y.B"), new KVPathUnifier[] { //
						unifierFromStrings("B", "B", "", "", "y"), //
				} }, //

				// Strong cases
				{ pathFromString("A.y  "), pathFromString("y"), new KVPathUnifier[] {} }, //
				{ pathFromString("  y.A"), pathFromString("y"), new KVPathUnifier[] {} }, //
				{ pathFromString("A.y.A"), pathFromString("y"), new KVPathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("y"), new KVPathUnifier[] {} }, //

				// Fixed cases
				{ pathFromString(".A.y"), pathFromString("y.B"), new KVPathUnifier[] { //
						unifierFromStrings("", "B", ".A", "", "y"), //
				} }, //
				{ pathFromString("A.y"), pathFromString("y.B."), new KVPathUnifier[] { //
						unifierFromStrings("", "B.", "A", "", "y"), //
				} }, //
				{ pathFromString(".A.y"), pathFromString("y.B."), new KVPathUnifier[] { //
						unifierFromStrings("", "B.", ".A", "", "y"), //
				} }, //

				// False fixed cases
				{ pathFromString(" A.y."), pathFromString(" y.B"), new KVPathUnifier[] {} }, //
				{ pathFromString(" A.y "), pathFromString(".y.B"), new KVPathUnifier[] {} }, //
				{ pathFromString(".A.y."), pathFromString(".y.B"), new KVPathUnifier[] {} }, //

		});
	}

	@ParameterizedTest
	@MethodSource("weakKVPathUnifiersSource")
	void weakKVPathUnifier(KVPath head, KVPath body, KVPathUnifier ref_unifiers[])
	{
		List<? extends KVPathUnifier> unifiers = UNIFIERS.weakUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (KVPathUnifier ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> strongKVPathUnifiersSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("A.y"), pathFromString("y"), new KVPathUnifier[] { //
						unifierFromStrings("", "", "A", "", "y"), //
				} }, //
				{ pathFromString("y.A"), pathFromString("y"), new KVPathUnifier[] { //
						unifierFromStrings("", "", "", "A", "y"), //
				} }, //
				{ pathFromString("A.y.A"), pathFromString("y"), new KVPathUnifier[] { //
						unifierFromStrings("", "", "A", "A", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("y"), new KVPathUnifier[] { //
						unifierFromStrings("", "", "", "", "y"), //
				} }, //
				// Weak cases
				{ pathFromString(" A.y  "), pathFromString("y.B"), new KVPathUnifier[] {} }, //
				{ pathFromString(".A.y  "), pathFromString("y."), new KVPathUnifier[] {} }, //
				{ pathFromString(" A.y  "), pathFromString("y."), new KVPathUnifier[] {} }, //
				{ pathFromString("  y.A"), pathFromString("B.y  "), new KVPathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("  y.B"), new KVPathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("B.y  "), new KVPathUnifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("B.y.B"), new KVPathUnifier[] {} }, //

				// Fixed cases
				{ pathFromString(".A.y"), pathFromString("y"), new KVPathUnifier[] { //
						unifierFromStrings("", "", ".A", "", "y"), //
				} }, //
				// False fixed cases
				{ pathFromString("A.y"), pathFromString(".y"), new KVPathUnifier[] {} }, //
		});
	}

	@ParameterizedTest
	@MethodSource("strongKVPathUnifiersSource")
	void strongKVPathUnifier(KVPath head, KVPath body, KVPathUnifier ref_unifiers[])
	{
		List<? extends KVPathUnifier> unifiers = UNIFIERS.strongUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (KVPathUnifier ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}
}
