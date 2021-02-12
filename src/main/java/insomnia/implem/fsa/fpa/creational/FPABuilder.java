package insomnia.implem.fsa.fpa.creational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.AbstractSimpleGFPA;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.fsa.edge.FSAEdge;
import insomnia.implem.fsa.fpa.FPAProperties;
import insomnia.implem.fsa.fpa.FPAs;
import insomnia.implem.fsa.state.FSAState;

public final class FPABuilder<VAL, LBL>
{
	boolean mustBeSync;
	boolean createNewStates;

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
		IGFPA<VAL, LBL> gfpa       = this.gfpa;
		boolean         mustBeSync = this.mustBeSync && !gfpa.getProperties().isSynchronous();

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
		for (IFSAEdge<VAL, LBL> edge : gfpa.getAllEdges())
			edges.add(new FSAEdge<>(newStatesMap.get(edge.getParent()), newStatesMap.get(edge.getChild()), edge.getLabelCondition()));

		return FPAs.create(new AbstractSimpleGFPA<VAL, LBL>(newStatesMap.values(), rootedStates, terminalStates, initialStates, finalStates, edges, gfpa.getProperties())
		{
		});
	}

	// =========================================================================

	private IGFPA<VAL, LBL> createSync(IGFPA<VAL, LBL> gfpa)
	{
		Collection<IFSAEdge<VAL, LBL>> edges = new ArrayList<>(gfpa.getEdges());

		Collection<IFSAState<VAL, LBL>> rootedStates, terminalStates, initialStates, finalStates;
		initialStates  = new HashSet<>();
		finalStates    = new HashSet<>();
		rootedStates   = new HashSet<>();
		terminalStates = new HashSet<>();

		// TODO: combine state value condition when needed
		for (IFSAState<VAL, LBL> state : gfpa.getStates())
		{
			Collection<IFSAState<VAL, LBL>> currentStates = new ArrayList<>(gfpa.getEpsilonClosure(state));

			if (gfpa.isInitial(state))
				initialStates.add(state);
			if (gfpa.isFinal(state))
				finalStates.add(state);
			if (gfpa.isRooted(state))
				rootedStates.add(state);
			if (gfpa.isTerminal(state))
				terminalStates.add(state);

			currentStates.remove(state);

			for (IFSAState<VAL, LBL> otherState : currentStates)
			{
				if (gfpa.isRooted(state))
					rootedStates.add(otherState);
				if (gfpa.isInitial(state))
					initialStates.add(otherState);

				if (gfpa.isTerminal(otherState))
					terminalStates.add(state);
				if (gfpa.isFinal(otherState))
					finalStates.add(state);
			}
			for (IFSAEdge<VAL, LBL> edge : gfpa.getEdgesOf(currentStates))
				edges.add(new FSAEdge<>(state, edge.getChild(), edge.getLabelCondition()));
		}
		IFPAProperties properties = new FPAProperties(false, true);
		return FPAs.create(new AbstractSimpleGFPA<VAL, LBL>(gfpa.getStates(), rootedStates, terminalStates, initialStates, finalStates, edges, properties)
		{
		});
	}
}
