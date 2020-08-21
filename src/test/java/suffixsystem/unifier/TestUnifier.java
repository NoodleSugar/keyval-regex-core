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
		return new Unifier(pathFromString(pb), pathFromString(sb), pathFromString(ph), pathFromString(sh));
	}

	static List<Object[]> computeSource()
	{
		return Arrays.asList(new Object[][] { //
				{ pathFromString("a.b"), pathFromString("b.a.b.a"), new Unifier[] { //
						unifierFromStrings("a", "", "", "a.b.a"), //
						unifierFromStrings("", "b", "b.a.b", ""), //
						unifierFromStrings("", "", "b", "a"), //
				} }, //
				{ pathFromString("a.a"), pathFromString("a.a.a"), new Unifier[] { //
						unifierFromStrings("a", "", "", "a.a"), //
						unifierFromStrings("", "", "", "a"), //
						unifierFromStrings("", "", "a", ""), //
						unifierFromStrings("", "a", "a.a", ""), //
				} }, //
		});

	}

	@ParameterizedTest
	@MethodSource("computeSource")
	void compute(Path body, Path head, Unifier ref_unifiers[])
	{
		List<Unifier> unifiers = Unifier.compute(body, head);
		assertEquals(ref_unifiers.length, unifiers.size());

		for(Unifier ref : ref_unifiers)
			assertTrue(unifiers.contains(ref));
	}
}
