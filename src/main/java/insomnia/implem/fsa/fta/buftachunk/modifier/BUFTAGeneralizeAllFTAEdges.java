package insomnia.implem.fsa.fta.buftachunk.modifier;

import java.util.ArrayList;
import java.util.List;

import insomnia.fsa.IFSAState;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.lib.help.HelpLists;

/**
 * Generalize all hyper-transition of an automaton.
 * <p>
 * The modifier transform the automaton to recognize all the more general trees than all in its actual language.
 * Note that trees are considered as non-ordered.
 * </p>
 * <p>
 * For example, if a hyper-transition recognize the tree a(b,c,d),
 * than the generalization must generate new transitions to recognize all the more general trees;
 * that is for this example:
 * <ul>
 * <li>a(b,c),a.d</li>
 * <li>a.b,a(c,d)</li>
 * <li>a(b,d),a.c</li>
 * <li>a.b,a.c,a.d</li>
 * </ul>
 * </p>
 * 
 * @author zuri
 */
public final class BUFTAGeneralizeAllFTAEdges<VAL, LBL> implements IBUFTAChunkModifier<VAL, LBL>
{
	private BUFTAGeneralizeAllFTAEdges()
	{
	}

	private enum S
	{
		INSTANCE;

		private final BUFTAGeneralizeAllFTAEdges<?, ?> val = new BUFTAGeneralizeAllFTAEdges<>();
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> BUFTAGeneralizeAllFTAEdges<VAL, LBL> create()
	{
		return (BUFTAGeneralizeAllFTAEdges<VAL, LBL>) S.INSTANCE.val;
	}

	// =========================================================================

	@SuppressWarnings("unused")
	@Override
	public void accept(BUFTAChunk<VAL, LBL> automaton, Environment<VAL, LBL> env)
	{
		var gc = automaton.getGChunk();

		for (var ftaEdge : new ArrayList<>(automaton.getFTAEdges()))
		{
			var parents = ftaEdge.getParents();

			if (parents.size() <= 1)
				continue;

			var child = ftaEdge.getChild();

			List<IFSAState<VAL, LBL>> parentsNewNodes = new ArrayList<>(parents.size());

			var edges = gc.getEdgesOf(child);

			for (var simpleEdge : edges)
			{
				var labelCondition = simpleEdge.getLabelCondition();

				for (var p : parents)
				{
					env.addChildFTAEdge(automaton, p);
					var newState = gc.copyState(child);
					env.addEdge(automaton, p, newState, labelCondition);
					parentsNewNodes.add(newState);
				}

				Iterable<List<Integer>> powerSet = () -> HelpLists.ipowerSetAsStream(parentsNewNodes).iterator();

				for (var partOfParents : powerSet)
				{
					var remainingParents = HelpLists.excludeAll(parents, partOfParents, true);
					var unfoldParents    = HelpLists.getAll(parentsNewNodes, partOfParents);

					// Case take into account with all elements
					if (remainingParents.size() == 1)
						continue;
					if (remainingParents.isEmpty())
					{
						var newFTAEdge = new FTAEdge<>(unfoldParents, simpleEdge.getChild(), FTAEdgeConditions.getInclusiveFactory());
						automaton.addFTAEdge(newFTAEdge);
					}
					else
					{
						var remainingState   = gc.createState();
						var remainingFTAEdge = new FTAEdge<>(remainingParents, remainingState, FTAEdgeConditions.getInclusiveFactory());
						var remainingChild   = gc.copyState(simpleEdge.getChild());

						automaton.addFTAEdge(remainingFTAEdge);
						automaton.addEdge(remainingState, remainingChild, labelCondition);

						if (true)
						{ // Recursive hyper-edges
							var unfoldState   = gc.createState();
							var unfoldFTAEdge = new FTAEdge<>(unfoldParents, unfoldState, FTAEdgeConditions.getInclusiveFactory());

							automaton.addFTAEdge(unfoldFTAEdge);
							var mergeFTAEdge = new FTAEdge<>(List.of(remainingChild, unfoldState), simpleEdge.getChild(), ftaEdge.getConditionFactory());
							automaton.addFTAEdge(mergeFTAEdge);
						}
						else
						{ // Direct method
							var newParents = new ArrayList<IFSAState<VAL, LBL>>();
							newParents.add(remainingChild);
							newParents.addAll(unfoldParents);

							var mergeFTAEdge = new FTAEdge<VAL, LBL>(newParents, simpleEdge.getChild(), ftaEdge.getConditionFactory());

							automaton.addFTAEdge(mergeFTAEdge);
						}
					}
				}
			}
		}
	}
}
