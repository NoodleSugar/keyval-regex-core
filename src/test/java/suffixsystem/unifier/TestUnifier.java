package suffixsystem.unifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.rule.tree.Path;
import insomnia.rule.tree.Paths;
import insomnia.suffixsystem.unifier.Unifier;

class TestUnifier
{
	public static Path pathFromString(String p)
	{
		return Paths.pathFromString(p);
	}

	public static Unifier unifierFromStrings(String pb, String sb, String ph, String sh)
	{
		return unifierFromStrings(pb, sb, ph, sh, "");
	}

	public static Unifier unifierFromStrings(String pb, String sb, String ph, String sh, String ref)
	{
		return new Unifier(pathFromString(pb), pathFromString(sb), pathFromString(ph), pathFromString(sh), pathFromString(ref));
	}

	static List<Object[]> computeSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("b.a.b.a"), pathFromString("a.b"), new Unifier[] { //
						unifierFromStrings("a", "", "", "a.b.a", "b"), //
						unifierFromStrings("", "b", "b.a.b", "", "a"), //
						unifierFromStrings("", "", "b", "a", "a.b"), //
				} }, //
				{ pathFromString("a.a.a"), pathFromString("a.a"), new Unifier[] { //
						unifierFromStrings("a", "", "", "a.a", "a"), //
						unifierFromStrings("", "", "", "a", "a.a"), //
						unifierFromStrings("", "", "a", "", "a.a"), //
						unifierFromStrings("", "a", "a.a", "", "a"), //
				} }, //
		});

	}

	@ParameterizedTest
	@MethodSource("computeSource")
	void compute(Path head, Path body, Unifier ref_unifiers[])
	{
		List<Unifier> unifiers = Unifier.compute(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (Unifier ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> weakUnifiersSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("A.y"), pathFromString("y.B"), new Unifier[] { //
						unifierFromStrings("", "B", "A", "", "y"), //
				} }, //
				{ pathFromString("y.A"), pathFromString("B.y"), new Unifier[] { //
						unifierFromStrings("B", "", "", "A", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("y.B"), new Unifier[] { //
						unifierFromStrings("", "B", "", "", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("B.y"), new Unifier[] { //
						unifierFromStrings("B", "", "", "", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("B.y.B"), new Unifier[] { //
						unifierFromStrings("B", "B", "", "", "y"), //
				} }, //

				// Strong cases
				{ pathFromString("A.y  "), pathFromString("y"), new Unifier[] {} }, //
				{ pathFromString("  y.A"), pathFromString("y"), new Unifier[] {} }, //
				{ pathFromString("A.y.A"), pathFromString("y"), new Unifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("y"), new Unifier[] {} }, //
		});
	}

	@ParameterizedTest
	@MethodSource("weakUnifiersSource")
	void weakUnifier(Path head, Path body, Unifier ref_unifiers[])
	{
		List<Unifier> unifiers = Unifier.weakUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (Unifier ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}

	static List<Object[]> strongUnifiersSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("A.y"), pathFromString("y"), new Unifier[] { //
						unifierFromStrings("", "", "A", "", "y"), //
				} }, //
				{ pathFromString("y.A"), pathFromString("y"), new Unifier[] { //
						unifierFromStrings("", "", "", "A", "y"), //
				} }, //
				{ pathFromString("A.y.A"), pathFromString("y"), new Unifier[] { //
						unifierFromStrings("", "", "A", "A", "y"), //
				} }, //
				{ pathFromString("y"), pathFromString("y"), new Unifier[] { //
						unifierFromStrings("", "", "", "", "y"), //
				} }, //
				// Weak cases
				{ pathFromString("A.y  "), pathFromString("  y.B"), new Unifier[] {} }, //
				{ pathFromString("  y.A"), pathFromString("B.y  "), new Unifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("  y.B"), new Unifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("B.y  "), new Unifier[] {} }, //
				{ pathFromString("  y  "), pathFromString("B.y.B"), new Unifier[] {} }, //
		});
	}

	@ParameterizedTest
	@MethodSource("strongUnifiersSource")
	void strongUnifier(Path head, Path body, Unifier ref_unifiers[])
	{
		List<Unifier> unifiers = Unifier.strongUnifiers(head, body);
		assertEquals(ref_unifiers.length, unifiers.size());

		for (Unifier ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}
}
