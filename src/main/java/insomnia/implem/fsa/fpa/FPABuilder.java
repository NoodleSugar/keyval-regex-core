package insomnia.implem.fsa.fpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.AbstractSimpleGFPA;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.fsa.edge.FSAEdge;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.fsa.state.FSAState;

public final class FPABuilder<VAL, LBL>
{
	boolean         mustBeSync;
	boolean         createNewStates;
	IGFPA<VAL, LBL> gfpa;

	public FPABuilder(IGFPA<VAL, LBL> gfpa)
	{
		this.gfpa = gfpa;
		clear();
	}

	// =========================================================================

	public void clear()
	{
		this.mustBeSync      = false;
		this.createNewStates = false;
	}

	/**
	 * Information that specify if the builded automaton must be synchronized or not.
	 * If that value is set to false but the internal automaton is already sync, then the builded automaton will be sync.
	 */
	public FPABuilder<VAL, LBL> mustBeSync(boolean mustBeSync)
	{
		this.mustBeSync = mustBeSync;
		return this;
	}

	public FPABuilder<VAL, LBL> createNewStates(boolean createNewStates)
	{
		this.createNewStates = createNewStates;
		return this;
	}

	public boolean getMustBeSync()
	{
		return mustBeSync;
	}

	public boolean getCreateNewStates()
	{
		return createNewStates;
	}

	// =========================================================================

	public IGFPA<VAL, LBL> create()
	{
		IGFPA<VAL, LBL> gfpa = this.gfpa;

		// Generate a copy of gfpa with new states and edges;
		if (createNewStates)
		{
			gfpa = copyClone(gfpa);

			if (mustBeSync)
				return createSync(gfpa);

			return gfpa;
		}

		if (mustBeSync)
			return createSync(gfpa);

		return FPAs.create(gfpa);
	}

	private IGFPA<VAL, LBL> copyClone(IGFPA<VAL, LBL> gfpa)
	{
		Collection<IFSAEdge<VAL, LBL>>  edges = new ArrayList<>();
		Collection<IFSAState<VAL, LBL>> rootedStates, terminalStates, initialStates, finalStates;

		Map<IFSAState<VAL, LBL>, IFSAState<VAL, LBL>> newStatesMap = new HashMap<>();

		initialStates  = new ArrayList<>(gfpa.getInitialStates().size());
		finalStates    = new ArrayList<>(gfpa.getFinalStates().size());
		rootedStates   = new ArrayList<>(gfpa.getRootedStates().size());
		terminalStates = new ArrayList<>(gfpa.getTerminalStates().size());

		// Init states
		for (IFSAState<VAL, LBL> state : gfpa.getStates())
		{
			IFSAState<VAL, LBL> newState = new FSAState<>(state.getValueCondition());
			newStatesMap.put(state, newState);

			if (gfpa.isInitial(state))
				initialStates.add(newState);
			if (gfpa.isFinal(state))
				finalStates.add(newState);
			if (gfpa.isRooted(state))
				rootedStates.add(newState);
			if (gfpa.isTerminal(state))
				terminalStates.add(newState);
		}

		// Add edges
		for (IFSAEdge<VAL, LBL> edge : gfpa.getEdges())
			edges.add(new FSAEdge<>(newStatesMap.get(edge.getParent()), newStatesMap.get(edge.getChild()), edge.getLabelCondition()));

		return FPAs.create(new AbstractSimpleGFPA<VAL, LBL>(newStatesMap.values(), rootedStates, terminalStates, initialStates, finalStates, edges, gfpa.getProperties())
		{
		});
	}

	// =========================================================================

	private IGFPA<VAL, LBL> createSync(IGFPA<VAL, LBL> gfpa)
	{
		Collection<IFSAEdge<VAL, LBL>> edges      = new ArrayList<>();
		Collection<IFSAEdge<VAL, LBL>> addedEdges = new ArrayList<>();

		Queue<Collection<IFSAState<VAL, LBL>>> listOfNextStates = new LinkedList<>();

		Collection<IFSAState<VAL, LBL>> processedStates = new HashSet<>();
		Collection<IFSAState<VAL, LBL>> states, rootedStates, terminalStates, initialStates, finalStates;

		listOfNextStates.add(gfpa.epsilonClosure(gfpa.getInitialStates()));
		states         = new HashSet<>();
		initialStates  = new ArrayList<>();
		finalStates    = new HashSet<>();
		rootedStates   = new HashSet<>();
		terminalStates = new HashSet<>();

		boolean isInitial = true;

		// TODO: combine state value condition when needed
		while (!listOfNextStates.isEmpty())
		{
			Collection<IFSAState<VAL, LBL>> currentStates = listOfNextStates.poll();

			boolean isRooted   = currentStates.stream().allMatch(s -> gfpa.isRooted(s));
			boolean isTerminal = currentStates.stream().allMatch(s -> gfpa.isTerminal(s));
			boolean isFinal    = currentStates.stream().anyMatch(s -> gfpa.isFinal(s));
			currentStates.removeAll(processedStates);

			for (IFSAEdge<VAL, LBL> currentEdge : gfpa.getEdges(currentStates))
			{
				if (FSALabelConditions.isEpsilonCondition(currentEdge.getLabelCondition()))
				{
					Collection<IFSAState<VAL, LBL>> subStates = gfpa.epsilonClosure(currentEdge.getChild());
					Collection<IFSAEdge<VAL, LBL>>  subEdges  = gfpa.getEdges(subStates);

					for (IFSAEdge<VAL, LBL> subEdge : subEdges)
					{
						// Skip epsilon edges (not needed)
						if (FSALabelConditions.isEpsilonCondition(subEdge.getLabelCondition()))
							continue;

						addedEdges.add(new FSAEdge<>(currentEdge.getParent(), subEdge.getChild(), subEdge.getLabelCondition()));
					}
				}
				else
					addedEdges.add(currentEdge);
			}

			if (isRooted)
				rootedStates.addAll(currentStates);
			if (isTerminal)
				terminalStates.addAll(currentStates);
			if (isInitial)
				initialStates.addAll(currentStates);
			if (isFinal)
				finalStates.addAll(currentStates);

			for (IFSAEdge<VAL, LBL> edge : addedEdges)
			{
				edges.add(edge);
				states.add(edge.getParent());
				states.add(edge.getChild());

				if (!processedStates.contains(edge.getChild()))
					listOfNextStates.add(gfpa.epsilonClosure(edge.getChild()));
			}
			addedEdges.clear();
			processedStates.addAll(currentStates);
			isInitial = false;
		}
		IFPAProperties properties = new FPAProperties(false, true);

		return FPAs.create(new AbstractSimpleGFPA<VAL, LBL>(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties)
		{
		});
	}
}
