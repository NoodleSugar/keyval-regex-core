package insomnia.implem.fsa.fta.buftachunk.modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSAState;
import insomnia.implem.fsa.fta.BUFTAMatchers;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.unifier.SemiTwigUnifiers;
import insomnia.rule.IRule;

/**
 * Apply rules to a graph chunk.
 * 
 * @author zuri
 */
public final class BUFTATerminalRuleApplier<VAL, LBL>
{
	private BUFTATerminalRuleApplier()
	{
		throw new AssertionError();
	}

	// ==========================================================================

	static private class Modifier<VAL, LBL> implements IBUFTAChunkModifier<VAL, LBL>
	{
		private Collection<IRule<VAL, LBL>> rules;

		private Environment<VAL, LBL> env;

		Modifier(Iterable<? extends IRule<VAL, LBL>> rules)
		{
			this.rules = CollectionUtils.collect(rules, r -> {

				if (!r.frontierIsTerminal())
					throw new IllegalArgumentException(String.format("A rule must be frontier terminal, have: %s", r));

				return r;
			});
		}

		private Map<INode<VAL, LBL>, BUFTAChunk<VAL, LBL>> bodyAutomaton;

		private Queue<BUFTAChunk<VAL, LBL>> automata;

		@Override
		public void accept(BUFTAChunk<VAL, LBL> chunk, Environment<VAL, LBL> env)
		{
			bodyAutomaton = new HashMap<>();
			automata      = new LinkedList<>();
			automata.add(chunk);
			this.env = env;
			apply();
		}

		private BUFTAChunk<VAL, LBL> getAutomatonOf(ITree<VAL, LBL> tree)
		{
			BUFTAChunk<VAL, LBL> ret = bodyAutomaton.get(tree.getRoot());

			if (null == ret)
			{
				ret = env.build(tree);
				bodyAutomaton.put(tree.getRoot(), ret);
				automata.add(ret);
			}
			return ret;
		}

		private void apply()
		{
			while (!automata.isEmpty())
			{
				var automaton    = automata.poll();
				var query        = automaton.getTree();
				var automatonRef = automaton.copyClone();

				for (var rule : rules)
				{
					var unifiers = SemiTwigUnifiers.compute(rule, query);

					if (unifiers.isEmpty())
						continue;

					var matchedBodyStates = BUFTAMatchers.getPreFTAMatchingStates(automatonRef, rule.getBody());

					for (var unifier : unifiers)
					{
						var semiTwig              = unifier.treeSemiTwig();
						var semiTwigMatchedStates = BUFTAMatchers.getPreFTAMatchingStates(automatonRef, semiTwig);
						var rootNode              = semiTwig.getRoot();
						var rootStates            = automatonRef.getNodeStates(rootNode);

						var bodyAutomaton           = getAutomatonOf(rule.getBody());
						var bodyAutomatonGChunk     = bodyAutomaton.getGChunk();
						var bodyAutomatonRootStates = bodyAutomaton.getNodeStates(rule.getBody().getRoot());
						automaton.union(bodyAutomaton);

						for (var treeAutomatonRootState : bodyAutomatonRootStates)
						{
							bodyAutomatonGChunk.setFinal(treeAutomatonRootState, false);

							for (var rootState : rootStates)
							{
								var ftaEdges = CollectionUtils.select(automatonRef.getFTAEdgesTo(rootState), //
									e -> !CollectionUtils.intersection(e.getParents(), semiTwigMatchedStates).isEmpty());

								for (var ftaEdge : ftaEdges)
								{
									List<IFSAState<VAL, LBL>> newParents = new ArrayList<>(ftaEdge.getParents());

									// Avoid redundancy
									if (CollectionUtils.containsAny(newParents, matchedBodyStates))
										newParents.removeIf(s -> semiTwigMatchedStates.contains(s));
									else
										newParents = CollectionUtils.collect(newParents, //
											s -> semiTwigMatchedStates.contains(s) ? treeAutomatonRootState : s, //
											new ArrayList<>());

									automaton.addFTAEdge(new FTAEdge<VAL, LBL>(newParents, rootState, ftaEdge.getCondition()));
								}
							}
						}
					}
				}
			}
		}
	}

	public static <VAL, LBL> IBUFTAChunkModifier<VAL, LBL> getChunkModifier(Iterable<? extends IRule<VAL, LBL>> rules)
	{
		return new Modifier<>(rules);
	}
	// ==========================================================================
}
