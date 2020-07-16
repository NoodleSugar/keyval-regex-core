package rule.ontology;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import insomnia.rule.PathRule;
import insomnia.rule.ontology.PathGRD;
import insomnia.rule.ontology.PathOntology;
import insomnia.rule.ontology.IGRD.DependencyMode;

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
		rules.add(new PathRule("a", "b"));
		rules.add(new PathRule("a.b.c", "d"));
		rules.add(new PathRule("b.c", "e"));
		rules.add(new PathRule("b", "d"));
		rules.add(new PathRule("c.e", "a"));

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
}
