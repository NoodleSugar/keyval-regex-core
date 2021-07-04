package insomnia.fsa.fta;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;

public enum BUFTAProperty
{
	/**
	 * Horizontal deterministic.
	 * <p>
	 * Present if it exists an hyper-edge that is an input of another one (possibly by an ε-path).
	 */
	HYPER_RECURSION(BUFTAProperty::hyperRecursion);

	// ==========================================================================

	private Predicate<IBUFTA<?, ?>> predicate;

	BUFTAProperty(Predicate<IBUFTA<?, ?>> predicate)
	{
		this.predicate = predicate;
	}

	public Predicate<IBUFTA<?, ?>> getPredicate()
	{
		return predicate;
	}

	// ==========================================================================

	public static EnumSet<BUFTAProperty> checkProperties(IBUFTA<?, ?> automaton, EnumSet<BUFTAProperty> check)
	{
		EnumSet<BUFTAProperty> ret = EnumSet.noneOf(BUFTAProperty.class);
		check.stream().filter(s -> s.getPredicate().test(automaton)).forEach(ret::add);
		return ret;
	}

	public static EnumSet<BUFTAProperty> checkProperties(IBUFTA<?, ?> automaton)
	{
		return checkProperties(automaton, EnumSet.copyOf(List.of(BUFTAProperty.values())));
	}

	static public void union(EnumSet<BUFTAProperty> dest, EnumSet<BUFTAProperty> src)
	{
		dest.retainAll(CollectionUtils.intersection(dest, src));
	}

	// ==========================================================================

	private static <VAL, LBL> boolean hyperRecursion(IBUFTA<VAL, LBL> automaton)
	{
		Set<IFSAState<VAL, LBL>>     tested = new HashSet<>();
		var                          gfpa   = automaton.getGFPA();
		Iterable<IFTAEdge<VAL, LBL>> hedges = () -> automaton.getFTAEdges().stream().filter(IFTAEdge::isNotPathEdge).iterator();

		for (var hedge : hedges)
		{
			var     childs     = CollectionUtils.removeAll(IGFPA.getEpsilonClosureOf(gfpa, Collections.singleton(hedge.getChild())), tested);
			boolean injectable = automaton.getFTAEdges().stream() //
				.filter(IFTAEdge::isNotPathEdge) //
				.anyMatch(e -> CollectionUtils.containsAny(childs, e.getParents()));

			if (injectable)
				return true;

			tested.addAll(childs);
		}
		return false;
	}
}
