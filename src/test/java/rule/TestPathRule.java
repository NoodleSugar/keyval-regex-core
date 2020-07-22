package rule;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;

import insomnia.rule.PathRule;

class TestPathRule
{
	// TODO ajouter des tests existentiels

	@ParameterizedTest
	@CsvSource({
			"a, b, b.c, d, false, true, false, false", //
			"d, c, a.c.huhu, a, false, true, false, true", //
			"a.b.c, d, b, e.a, true, false, false , false", // prefix-suffix
			"a.b.c, d, b, c.e, true, false, false, false", // prefix-suffix
			"a, b, a.b, d, false, false, false, false, true, false, false, false", // rooted
			"a, b, b.c, d, false, false, false, false, false, true, false, false", // valued
			"a.b, b, b, d, false, true, false, false, true, true, false, false", // rooted-valued
	})
	void weaks(ArgumentsAccessor args)
	{
		PathRule r1, r2;
		if(args.size() == 8)
		{
			r1 = new PathRule(args.getString(0), args.getString(1));
			r2 = new PathRule(args.getString(2), args.getString(3));
		}
		else
		{
			r1 = PathRule.create(args.getString(0), args.getString(1), args.getBoolean(8), args.getBoolean(9));
			r2 = PathRule.create(args.getString(2), args.getString(3), args.getBoolean(10), args.getBoolean(11));
		}
		assertTrue(r1.dependsWeakOn(r2) == args.getBoolean(4));
		assertTrue(r2.dependsWeakOn(r1) == args.getBoolean(5));
		assertTrue(r1.dependsWeakOn(r1) == args.getBoolean(6));
		assertTrue(r2.dependsWeakOn(r2) == args.getBoolean(7));
	}

	@ParameterizedTest
	@CsvSource({
			"a, b.c, c, d, false, true, false, false", //
			"c, hoho.b.c, b, d, false, true, true, false", //
			"a, b.c, c, c.e, false, false, false, true, false, false, true, false", // rooted
			"a, c.d, c, e.c, false, false, false, true, false, false, false, true", // valued
			"a, c, c, e, false, true, false, false, false, false, true, true" // rooted-valued
	})
	void strongs(ArgumentsAccessor args)
	{
		PathRule r1, r2;
		if(args.size() == 8)
		{
			r1 = new PathRule(args.getString(0), args.getString(1));
			r2 = new PathRule(args.getString(2), args.getString(3));
		}
		else
		{
			r1 = PathRule.create(args.getString(0), args.getString(1), args.getBoolean(8), args.getBoolean(9));
			r2 = PathRule.create(args.getString(2), args.getString(3), args.getBoolean(10), args.getBoolean(11));
		}
		assertTrue(r1.dependsStrongOn(r2) == args.getBoolean(4));
		assertTrue(r2.dependsStrongOn(r1) == args.getBoolean(5));
		assertTrue(r1.dependsStrongOn(r1) == args.getBoolean(6));
		assertTrue(r2.dependsStrongOn(r2) == args.getBoolean(7));
	}
}
