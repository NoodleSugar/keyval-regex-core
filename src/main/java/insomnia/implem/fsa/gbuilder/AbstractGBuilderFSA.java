package insomnia.implem.fsa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import insomnia.fsa.AbstractGFSAutomaton;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAElement;
import insomnia.fsa.IFSAProperties;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IGFSAutomaton;
import insomnia.fsa.algorithm.IGFSAValidation;

public abstract class AbstractGBuilderFSA<VAL, LBL> //
	extends AbstractGFSAutomaton<VAL, LBL> //
	implements IGFSAutomaton<VAL, LBL>
{
	private IFSAProperties properties;

	private IGFSAValidation<VAL, LBL> validator;

	private Collection<IFSAState<VAL, LBL>> rootedStates;
	private Collection<IFSAState<VAL, LBL>> terminalStates;

	private Collection<IFSAState<VAL, LBL>> initialStates;
	private Collection<IFSAState<VAL, LBL>> finalStates;
	private Collection<IFSAState<VAL, LBL>> states;
	private Collection<IFSAEdge<VAL, LBL>>  edges;

	protected AbstractGBuilderFSA( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> rootedStates, //
		Collection<IFSAState<VAL, LBL>> terminalStates, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFSAProperties properties, //
		IGFSAValidation<VAL, LBL> validator //
	)
	{
		this.edges         = edges;
		this.states        = states;
		this.initialStates = initialStates;
		this.finalStates   = finalStates;
		this.properties    = properties;
		this.validator     = validator;

		this.rootedStates   = rootedStates;
		this.terminalStates = terminalStates;
	}

	@Override
	protected boolean isRooted(IFSAState<VAL, LBL> state)
	{
		return rootedStates.contains(state);
	}

	@Override
	protected boolean isTerminal(IFSAState<VAL, LBL> state)
	{
		return terminalStates.contains(state);
	}

	@Override
	public boolean test(IFSAElement<VAL, LBL> element)
	{
		return validator.test(this, element);
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
	public IFSAProperties getProperties()
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
		s1.append("Nodes: ").append(states).append("\n");
		s1.append("Edges:\n");

		for (IFSAEdge<VAL, LBL> edge : edges)
			s1.append(edge).append("\n");

		return s1.toString();
	}
}
