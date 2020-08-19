package rule.ontology;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;

import insomnia.rule.PathRule;
import insomnia.rule.ontology.PathGRD;
import insomnia.rule.ontology.PathOntology;
import insomnia.rule.ontology.IGRD.DependencyMode;
import insomnia.rule.tree.Path;

class TestPathGRD
{
	static List<PathRule> rules;
	static PathOntology ontology;
	static PathGRD grd;

	@BeforeAll
	static void init()
	{
		//TODO ajouter des règles intéréssantes
		rules = new ArrayList<>();
		rules.add(PathRule.create("a", "b"));
		rules.add(PathRule.create("a.b.c", "d"));
		rules.add(PathRule.create("b.c", "e"));
		rules.add(PathRule.create("b", "d"));
		rules.add(PathRule.create("c.e", "a"));

		ontology = new PathOntology(rules);
		grd = new PathGRD(ontology);
	}

	@Test
	void weakChildren()
	{
		List<PathRule> c = grd.getChildren(rules.get(0), DependencyMode.WEAK);
		assertEquals(3, c.size());
		assertTrue(c.contains(rules.get(1)));
		assertTrue(c.contains(rules.get(2)));
		assertTrue(c.contains(rules.get(3)));
	}

	@Test
	void weakParents()
	{
		List<PathRule> p = grd.getParents(rules.get(0), DependencyMode.WEAK);
		assertEquals(1, p.size());
		assertTrue(p.contains(rules.get(4)));
	}

	@Test
	void weakClosure()
	{
		List<PathRule> clos = grd.closure(rules.get(0), DependencyMode.WEAK);
		assertEquals(5, clos.size());
	}
	
	@Test
	void weakReverseClosure()
	{
		List<PathRule> clos = grd.reverseClosure(rules.get(0), DependencyMode.WEAK);
		assertEquals(3, clos.size());
		assertTrue(clos.contains(rules.get(0)));
		assertTrue(clos.contains(rules.get(2)));
		assertTrue(clos.contains(rules.get(4)));
	}
	
	@Test
	void strongChildren()
	{
		List<PathRule> c = grd.getChildren(rules.get(0), DependencyMode.STRONG);
		assertEquals(1, c.size());
		assertTrue(c.contains(rules.get(3)));
	}

	@Test
	void strongParents()
	{
		List<PathRule> p = grd.getParents(rules.get(0), DependencyMode.STRONG);
		assertEquals(1, p.size());
		assertTrue(p.contains(rules.get(4)));
	}
	
	@Test
	void strongClosure()
	{
		List<PathRule> clos = grd.closure(rules.get(0), DependencyMode.STRONG);
		assertEquals(2, clos.size());
		assertTrue(clos.contains(rules.get(0)));
		assertTrue(clos.contains(rules.get(3)));
	}
	
	@Test
	void strongReverseClosure()
	{
		List<PathRule> clos = grd.reverseClosure(rules.get(0), DependencyMode.STRONG);
		assertEquals(2, clos.size());
		assertTrue(clos.contains(rules.get(0)));
		assertTrue(clos.contains(rules.get(4)));
	}
	
	// TODO ajouter des tests existentiels
	// a -> il existe bc n'implique pas cd -> e

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
		void dependsWeak(ArgumentsAccessor args)
		{
			PathRule r1, r2;
			if(args.size() == 8)
			{
				r1 = PathRule.create(args.getString(0), args.getString(1));
				r2 = PathRule.create(args.getString(2), args.getString(3));
			}
			else
			{
				r1 = PathRule.create(args.getString(0), args.getString(1), args.getBoolean(8), args.getBoolean(9));
				r2 = PathRule.create(args.getString(2), args.getString(3), args.getBoolean(10), args.getBoolean(11));
			}
			assertTrue(grd.dependsWeakOn(r1, r2) == args.getBoolean(4));
			assertTrue(grd.dependsWeakOn(r2, r1) == args.getBoolean(5));
			assertTrue(grd.dependsWeakOn(r1,r1) == args.getBoolean(6));
			assertTrue(grd.dependsWeakOn(r2,r2) == args.getBoolean(7));
		}

		@ParameterizedTest
		@CsvSource({
				"a, b.c, c, d, false, true, false, false", //
				"c, hoho.b.c, b, d, false, true, true, false", //
				"a, b.c, c, c.e, false, false, false, true, false, false, true, false", // rooted
				"a, c.d, c, e.c, false, false, false, true, false, false, false, true", // valued
				"a, c, c, e, false, true, false, false, false, false, true, true" // rooted-valued
		})
		void dependStrong(ArgumentsAccessor args)
		{
			PathRule r1, r2;
			if(args.size() == 8)
			{
				r1 = PathRule.create(args.getString(0), args.getString(1));
				r2 = PathRule.create(args.getString(2), args.getString(3));
			}
			else
			{
				r1 = PathRule.create(args.getString(0), args.getString(1), args.getBoolean(8), args.getBoolean(9));
				r2 = PathRule.create(args.getString(2), args.getString(3), args.getBoolean(10), args.getBoolean(11));
			}
			assertTrue(grd.dependsStrongOn(r1,r2) == args.getBoolean(4));
			assertTrue(grd.dependsStrongOn(r2,r1) == args.getBoolean(5));
			assertTrue(grd.dependsStrongOn(r1,r1) == args.getBoolean(6));
			assertTrue(grd.dependsStrongOn(r2,r2) == args.getBoolean(7));
		}
		
		@ParameterizedTest
		@CsvSource({
				"a.b, false, false", //
		})
		void queryDependencies(ArgumentsAccessor args)
		{
			Path q = new Path(args.getBoolean(1), args.getBoolean(2), args.getString(0));
			List<PathRule> deps = grd.getQueryDependencies(q);
			
			assertEquals(2, deps.size());
			assertTrue(deps.contains(rules.get(0)));
			assertTrue(deps.contains(rules.get(4)));
		}
}
