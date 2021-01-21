package insomnia.implem.fsa.fpa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.AbstractGFPA;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.IGFPA;

public abstract class AbstractGBuilderFPA<VAL, LBL> //
	extends AbstractGFPA<VAL, LBL> //
	implements IGFPA<VAL, LBL>
{
	private IFPAProperties properties;

	private Collection<IFSAState<VAL, LBL>> rootedStates;
	private Collection<IFSAState<VAL, LBL>> terminalStates;

	private Collection<IFSAState<VAL, LBL>> initialStates;
	private Collection<IFSAState<VAL, LBL>> finalStates;
	private Collection<IFSAState<VAL, LBL>> states;
	private Collection<IFSAEdge<VAL, LBL>>  edges;

	protected AbstractGBuilderFPA( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFPAProperties properties //
	)
	{
		this.edges         = edges;
		this.states        = states;
		this.initialStates = initialStates;
		this.finalStates   = finalStates;
		this.properties    = properties;

		this.rootedStates   = rootedStates;
		this.terminalStates = terminalStates;
	}

	@Override
	public boolean isRooted(IFSAState<VAL, LBL> state)
	{
		return rootedStates.contains(state);
	}

	@Override
	public boolean isTerminal(IFSAState<VAL, LBL> state)
	{
		return terminalStates.contains(state);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getFinalStates()
	{
		return Collections.unmodifiableCollection(finalStates);
	}

	@Override
	public Collection<IFSAState<VAL, LBL>> getInitialStates()
	{
		return Collections.unmodifiableCollection(initialStates);
	}

	@Override
	public int nbStates()
	{
		return states.size();
	}

	@Override
	public IFPAProperties getProperties()
	{
		return properties;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges(Collection<? extends IFSAState<VAL, LBL>> states)
	{
		List<IFSAEdge<VAL, LBL>> ret = new ArrayList<>();

		for (IFSAState<VAL, LBL> _state : states)
		{
			IGBuilderState<VAL, LBL> state = (IGBuilderState<VAL, LBL>) _state;
			ret.addAll(state.getEdges());
		}
		return ret;
	}

	@Override
	public String toString()
	{
		StringBuffer s1 = new StringBuffer();

		s1.append("Initials: ").append(initialStates).append("\n");
		s1.append("Finals: ").append(finalStates).append("\n");
		s1.append("Rooted: ").append(rootedStates).append("\n");
		s1.append("Terminals: ").append(terminalStates).append("\n");
		s1.append("Nodes: ").append(states).append("\n");
		s1.append("Edges:\n");

		for (IFSAEdge<VAL, LBL> edge : edges)
			s1.append(edge).append("\n");

		return s1.toString();
	}
}
