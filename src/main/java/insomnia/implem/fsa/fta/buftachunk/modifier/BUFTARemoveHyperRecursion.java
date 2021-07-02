package insomnia.implem.fsa.fta.buftachunk.modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.implem.fsa.fta.edge.FTAEdge;
import insomnia.implem.fsa.fta.edgeCondition.FTAEdgeConditions;
import insomnia.lib.graph.IGraphEdge;
import insomnia.lib.help.HelpLists;

/**
 * Remove all direct hyper-recursion of the automaton.
 * An hyper-edge is recursive if its child state is an input of another hyper-edge.
 * <p>
 * No loop are allowed as a recursion: an hyper-edge must not be an input of itself.
 * For now ε-transition are not allowed between input/output hyper-edge's states.
 * </p>
 * 
 * @author zuri
 */
public final class BUFTARemoveHyperRecursion<VAL, LBL> implements IBUFTAChunkModifier<VAL, LBL>
{
	private BUFTARemoveHyperRecursion()
	{
	}

	private enum S
	{
		INSTANCE;

		private final BUFTARemoveHyperRecursion<?, ?> val = new BUFTARemoveHyperRecursion<>();
	}

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> BUFTARemoveHyperRecursion<VAL, LBL> create()
	{
		return (BUFTARemoveHyperRecursion<VAL, LBL>) S.INSTANCE.val;
	}

	// =========================================================================

	@Override
	public void accept(BUFTAChunk<VAL, LBL> automaton, Environment<VAL, LBL> env)
	{
		var gc = automaton.getGChunk();

		Queue<IFTAEdge<VAL, LBL>> toProcess = new LinkedList<>( //
			automaton.getFTAEdges().stream().filter(e -> !IFTAEdge.isPathEdge(e)).collect(Collectors.toList()));

		while (!toProcess.isEmpty())
		{
			IFSAState<VAL, LBL>             injectableOriginalChild;
			Collection<IFSAState<VAL, LBL>> injectableChilds;
			Collection<IFSAEdge<VAL, LBL>>  eChildsTransitions;
			Collection<IFTAEdge<VAL, LBL>>  injectables; // All the hyper-edges where the child has an ε-transition path to the child of the current hyper-edge.

			Collection<IFTAEdge<VAL, LBL>> patients;

			// Init variables
			{
				var injectableFTAEdge = toProcess.poll();
				injectableOriginalChild = injectableFTAEdge.getChild();

				eChildsTransitions = IGFPA.getEEpsilonClosureTo(gc, Collections.singleton(injectableOriginalChild));
				var tmp_injectableChilds = IGraphEdge.getNodes(eChildsTransitions);

				if (tmp_injectableChilds.isEmpty())
					tmp_injectableChilds = Collections.singletonList(injectableOriginalChild);

				injectables = tmp_injectableChilds.stream().flatMap(e -> automaton.getFTAEdgesTo(e).stream()) //
					.filter(e -> !IFTAEdge.isPathEdge(e)) //
					.collect(Collectors.toList());

				injectableChilds = injectables.stream().map(e -> e.getChild()).collect(Collectors.toSet());

				var tmp = tmp_injectableChilds;
				patients = automaton.getFTAEdges().stream() //
					.filter(e -> !IFTAEdge.isPathEdge(e) && CollectionUtils.containsAny(tmp, e.getParents())) //
					.collect(Collectors.toList()) //
				;
			}

			if (patients.isEmpty())
				continue;

			for (var patient : patients)
			{
				List<Collection<IFTAEdge<VAL, LBL>>> combinatory = new ArrayList<>();

				var parents   = patient.getParents();
				int nbParents = parents.size();

				int[] parentPos = IntStream.range(0, nbParents) //
					.filter(i -> injectableChilds.contains(parents.get(i))) //
					.toArray();

				{
					int i = parentPos.length;
					while (i-- > 0)
						combinatory.add(injectables);
				}

				for (List<IFTAEdge<VAL, LBL>> replaceBy : HelpLists.cartesianProductIterable(combinatory))
				{
					var newParents = new ArrayList<IFSAState<VAL, LBL>>();

					for (int parent_i = 0, pp_i = 0; parent_i < nbParents; parent_i++)
					{
						if (parentPos[pp_i] == parent_i)
						{
							var replacer  = replaceBy.get(pp_i++);
							var injection = replacer.getParents();
							newParents.addAll(injection);
						}
						else
							newParents.add(parents.get(parent_i));
					}
					var newFTAEdge = new FTAEdge<>(newParents, patient.getChild(), FTAEdgeConditions.getInclusiveFactory()); // TODO not necessary inclusive factory ?

					if (!automaton.addFTAEdge(newFTAEdge))
						continue;

					toProcess.add(newFTAEdge);
				}
			}

			// Clean direct ε-transitions to the original FTA child
			if (!eChildsTransitions.isEmpty())
			{
				var todelete = gc.getEpsilonEdgesTo(injectableOriginalChild);

				if (!todelete.isEmpty())
				{
					todelete = CollectionUtils.intersection(todelete, eChildsTransitions);
					todelete.forEach(gc::removeEdge);
				}
			}

			// Delete hyper-edge where a parent was substituated
			{
				var deletable = patients.stream().filter(e -> !e.getParents().stream().anyMatch(p -> !gc.getAllEdgesTo(p).isEmpty()));
				deletable.forEach(automaton::removeFTAEdge);
			}
			// Delete injected hyper-edges
			{
				var deletable = injectables.stream();
				deletable.forEach(automaton::removeFTAEdge);
			}
		}
	}
}
