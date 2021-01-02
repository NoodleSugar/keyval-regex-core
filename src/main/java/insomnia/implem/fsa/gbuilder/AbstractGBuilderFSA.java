package insomnia.implem.fsa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import insomnia.fsa.AbstractGFSAutomaton;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAProperties;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IGFSAutomaton;
import insomnia.fsa.algorithm.IFSAAValidation;

public abstract class AbstractGBuilderFSA<VAL, LBL, ELMNT> //
	extends AbstractGFSAutomaton<VAL, LBL, ELMNT> //
	implements IGFSAutomaton<VAL, LBL, ELMNT>
{
	private IFSAProperties properties;

	private IFSAAValidation<ELMNT, IGFSAutomaton<VAL, LBL, ELMNT>> validator;

	private Collection<IFSAState<VAL, LBL>> initialStates;
	private Collection<IFSAState<VAL, LBL>> finalStates;
	private Collection<IFSAState<VAL, LBL>> states;
	private Collection<IFSAEdge<VAL, LBL>>  edges;

	private boolean isRooted, isTerminal;

	protected AbstractGBuilderFSA( //
		Collection<IFSAState<VAL, LBL>> states, //
		Collection<IFSAState<VAL, LBL>> initialStates, //
		Collection<IFSAState<VAL, LBL>> finalStates, //
		Collection<IFSAEdge<VAL, LBL>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<ELMNT, IGFSAutomaton<VAL, LBL, ELMNT>> validator //
	)
	{
		this.edges         = edges;
		this.states        = states;
		this.initialStates = initialStates;
		this.finalStates   = finalStates;
		this.properties    = properties;
		this.validator     = validator;
		this.isRooted      = false;
		this.isTerminal    = false;
	}

	@Override
	public boolean test(ELMNT element)
	{
		return validator.test(this, element);
	}

	public void setRooted(boolean isRooted)
	{
		this.isRooted = isRooted;
	}

	public void setTerminal(boolean isTerminal)
	{
		this.isTerminal = isTerminal;
	}

	@Override
	protected boolean isRooted()
	{
		return isRooted;
	}

	@Override
	protected boolean isTerminal()
	{
		return isTerminal;
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

		s1.append("Rooted: ").append(isRooted).append("\n");
		s1.append("Terminal: ").append(isTerminal).append("\n");
		s1.append("Initials: ").append(initialStates).append("\n");
		s1.append("Finals: ").append(finalStates).append("\n");
		s1.append("Nodes: ").append(states).append("\n");
		s1.append("Edges:\n");

		for (IFSAEdge<VAL, LBL> edge : edges)
			s1.append(edge).append("\n");
		return s1.toString();
	}
}
