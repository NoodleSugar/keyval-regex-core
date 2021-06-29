package insomnia.implem.fsa.fta.buftachunk.modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.lib.help.HelpLists;

public class BUFTASpecializeAllFTAEdges<VAL, LBL> implements IBUFTAChunkModifier<VAL, LBL>
{
	/**
	 * Groups edges by their {@link IFSALabelCondition}
	 */
	private static class EdgeClass<VAL, LBL> implements Iterable<List<IFSAEdge<VAL, LBL>>>
	{
		private List<IFSAEdge<VAL, LBL>> edges;

		EdgeClass(IFSALabelCondition<LBL> labelCondition, Collection<IFSAEdge<VAL, LBL>> edges)
		{
			this.edges = new LinkedList<>(edges);
			this.edges.sort((x, y) -> IFSAState.compare(x.getParent(), y.getParent()));
		}

		/**
		 * Iterate trough all the subset of {@link IFSAEdge}s such as an each element of the subset project on another.
		 * Each subset represents {@link IFSAEdge} that may be merge together.
		 */
		@Override
		public Iterator<List<IFSAEdge<VAL, LBL>>> iterator()
		{
			return IteratorUtils.filteredIterator(HelpLists.powerSet(edges), //
				s -> s.size() > 1 && HelpLists.eachMatchAny(s, (x, y) -> IFSAState.hasProjection(x.getParent(), y.getParent())) //
			);
		}
	}

	private List<EdgeClass<VAL, LBL>> classes(Collection<IFSAEdge<VAL, LBL>> edgesTo)
	{
		var stream = HelpLists.disjointClassesAsStream(edgesTo, (x, y) -> {
			return IFSALabelCondition.equals(x.getLabelCondition(), y.getLabelCondition()) //
			;
		}) //
			.filter(e -> e.size() > 1) //
			.map(e -> new EdgeClass<VAL, LBL>(e.get(0).getLabelCondition(), e)) //
		;
		return stream.collect(Collectors.toList());
	}

	private List<List<Collection<IFSAEdge<VAL, LBL>>>> selectChildsEdges(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> childStates)
	{
		List<List<Collection<IFSAEdge<VAL, LBL>>>> ret = new ArrayList<>();

		for (var childState : childStates)
		{
			List<Collection<IFSAEdge<VAL, LBL>>> childEdges = new ArrayList<>();

			for (var ftaEdge : automaton.getFTAEdgesTo(childState))
				childEdges.add(automaton.getGChunk().getEdgesTo(ftaEdge.getParents()));

			ret.add(childEdges);
		}
		return ret;
	}

	private Iterable<List<IFSAState<VAL, LBL>>> getAllChildsEdgesCombinations(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> childStates)
	{
		var childsEdges = selectChildsEdges(automaton, childStates);

		return (Iterable<List<IFSAState<VAL, LBL>>>) () -> HelpLists.cartesianProductAsStream(childsEdges) //
			.map((e) -> {
				return e.stream().flatMap(x -> x.stream()).map(s -> s.getChild()).collect(Collectors.toList());
			}).iterator();
	}

	private class AddInformations
	{
		// All used in hashCode/equals with reflection
		@SuppressWarnings("unused")
		private IFSAState<VAL, LBL>             parent;
		@SuppressWarnings("unused")
		private Collection<IFSAState<VAL, LBL>> childStates;
		@SuppressWarnings("unused")
		private IFSALabelCondition<LBL>         label;

		AddInformations(IFSAState<VAL, LBL> parent, Collection<IFSAState<VAL, LBL>> childStates, IFSALabelCondition<LBL> label)
		{
			this.parent      = parent;
			this.childStates = childStates;
			this.label       = label;
		}

		@Override
		public boolean equals(Object obj)
		{
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public int hashCode()
		{
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	@Override
	public void accept(BUFTAChunk<VAL, LBL> automaton, Environment<VAL, LBL> env)
	{
		Set<AddInformations>      added    = new HashSet<>();
		var                       gchunk   = automaton.getGChunk();
		Queue<IFTAEdge<VAL, LBL>> ftaEdges = CollectionUtils.select( //
			automaton.getFTAEdges(), //
			e -> e.getParents().size() > 1, //
			new LinkedList<>());

		while (!ftaEdges.isEmpty())
		{
			var                       ftaEdge     = ftaEdges.poll();
			var                       edgesTo     = gchunk.getEdgesTo(ftaEdge.getParents());
			List<EdgeClass<VAL, LBL>> edgeClasses = classes(edgesTo);

			for (var edgeClass : HelpLists.cartesianProductIterable(edgeClasses))
			{
				Collection<IFSAState<VAL, LBL>> dropFTAEdgeParents = new ArrayList<>();

				for (var selectedEdges : edgeClass)
				{
					IFSALabelCondition<LBL>   labelCondition  = selectedEdges.get(0).getLabelCondition();
					List<IFSAState<VAL, LBL>> selectedChilds  = CollectionUtils.collect(selectedEdges, e -> e.getParent(), new ArrayList<>(selectedEdges.size()));
					List<IFSAState<VAL, LBL>> selectedParents = CollectionUtils.collect(selectedEdges, e -> e.getChild(), new ArrayList<>(selectedEdges.size()));
					dropFTAEdgeParents.addAll(selectedParents);

					var subState = gchunk.createMergeState(selectedChilds);
					selectedChilds = CollectionUtils.select(selectedChilds, s -> !gchunk.isInitial(s), new ArrayList<>());

					for (List<IFSAState<VAL, LBL>> childStatesCombination : getAllChildsEdgesCombinations(automaton, selectedChilds))
					{
						AddInformations addInfos = new AddInformations(ftaEdge.getChild(), childStatesCombination, labelCondition);

						if (added.contains(addInfos))
							continue;
						added.add(addInfos);

						var parentState = gchunk.copyState(subState);
						var newEdge     = env.addFTAEdge(automaton, childStatesCombination, parentState);

						var transitionState = gchunk.createState();
						gchunk.addEdge(parentState, transitionState, labelCondition);
						env.addFTAEdge(automaton, Collections.singletonList(transitionState), ftaEdge.getChild());

						ftaEdges.add(newEdge);
					}
				}
			}
		}
	}
}
