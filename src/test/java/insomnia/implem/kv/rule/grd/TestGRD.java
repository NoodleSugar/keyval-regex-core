package insomnia.implem.kv.rule.grd;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.MethodSource;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.rule.KVPathRules;
import insomnia.implem.kv.unifier.KVPathUnifiers;
import insomnia.implem.rule.dependency.AlphaDependencyValidation;
import insomnia.implem.rule.grd.creational.GRDFactory;
import insomnia.lib.help.HelpLists;
import insomnia.rule.IPathRule;
import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.dependency.IDependencyValidation;
import insomnia.rule.grd.IGRD;

public class TestGRD<E, V>
{
	static List<Object[]> alphaGRD() throws ParseException
	{
		List<Object[]> ret         = new ArrayList<>();
		List<Object[]> validations = new ArrayList<>();
		validations.add(new Object[] { new AlphaDependencyValidation<>(KVPathUnifiers.get()) });

		IPathRule<KVValue, KVLabel> a, b;

		// =====================================================================
		a = KVPathRules.fromString("a", "b.a");
		ret.add(new Object[] { //
				new Object[] { a }, //
				new Object[][] { { a, a } }, //
		});
		// =====================================================================
		a = KVPathRules.fromString("a", "b");
		b = KVPathRules.fromString("b.c", "d");
		ret.add(new Object[] { //
				new Object[] { a, b }, //
				new Object[][] { { a, b } }, //
		});
		// =====================================================================
		a = KVPathRules.fromString("a", "b");
		b = KVPathRules.fromString("b.c", "d.a");
		ret.add(new Object[] { //
				new Object[] { a, b }, //
				new Object[][] { { a, b }, { b, a } }, //
		});
		// =====================================================================

		// =====================================================================
		a = KVPathRules.fromString(".a", ".b.a");
		ret.add(new Object[] { //
				new Object[] { a }, //
				new Object[][] {}, //
		});
		// =====================================================================
		a = KVPathRules.fromString(".a", ".a.b");
		ret.add(new Object[] { //
				new Object[] { a }, //
				new Object[][] { { a, a } }, //
		});
		// =====================================================================
		a = KVPathRules.fromString(".a", ".a.b");
		ret.add(new Object[] { //
				new Object[] { a }, //
				new Object[][] { { a, a } }, //
		});
		// =====================================================================

		List<Pair<Object[], Object[]>> tmp = HelpLists.product(validations, ret);
		ret = HelpLists.mergePairsArrays(tmp);
		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ParameterizedTest
	@MethodSource
	void alphaGRD(ArgumentsAccessor args)
	{
		IDependencyValidation<KVValue, KVLabel> validation = args.get(0, IDependencyValidation.class);

		Collection<IRule<KVValue, KVLabel>>       rules    = new ArrayList<>();
		Collection<List<IRule<KVValue, KVLabel>>> expected = new HashSet<>();

		// Conversion stuff
		for (Object obj : (Object[]) args.get(1))
			rules.add((IRule) obj);

		for (Object[] objs : (Object[][]) args.get(2))
		{
			List<IRule<KVValue, KVLabel>> elist = new ArrayList();

			for (Object obj : objs)
				elist.add((IRule<KVValue, KVLabel>) obj);

			expected.add(elist);
		}

		IGRD<KVValue, KVLabel> grd = new GRDFactory(rules, validation).create();

		for (IDependency<KVValue, KVLabel> edge : grd.edgeSet())
		{
			List<IRule> tmp = new ArrayList(Arrays.asList(grd.getEdgeSource(edge), grd.getEdgeTarget(edge)));

			if (!expected.contains(tmp))
				fail(tmp + " not expected");
		}
		for (List<IRule<KVValue, KVLabel>> exp : expected)
		{
			if (!grd.containsEdge(exp.get(0), exp.get(1)))
				fail("Expected " + exp);
		}
		assertTrue(true);
	}
}
