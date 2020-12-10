package fsa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fsa.AbstractGFSAutomaton;
import fsa.FSAException;
import fsa.IFSAEdge;
import fsa.IFSAProperties;
import fsa.IFSAState;
import fsa.IGFSAutomaton;
import fsa.algorithm.IFSAAValidation;

public abstract class AbstractGBuilderFSA<E> extends AbstractGFSAutomaton<E> implements IGFSAutomaton<E>
{
	private IFSAProperties properties;

	private IFSAAValidation<E, IGFSAutomaton<E>> validator;

	private Collection<IFSAState<E>> initialStates;
	private Collection<IFSAState<E>> finalStates;
	private Collection<IFSAState<E>> states;
	private Collection<IFSAEdge<E>>  edges;

	protected AbstractGBuilderFSA( //
		Collection<IFSAState<E>> states, //
		Collection<IFSAState<E>> initialStates, //
		Collection<IFSAState<E>> finalStates, //
		Collection<IFSAEdge<E>> edges, //
		IFSAProperties properties, //
		IFSAAValidation<E, IGFSAutomaton<E>> validator //
	)
	{
		this.edges         = edges;
		this.states        = states;
		this.initialStates = initialStates;
		this.finalStates   = finalStates;
		this.properties    = properties;
		this.validator     = validator;
	}

	@Override
	public boolean test(List<E> path) throws FSAException
	{
		return validator.test(this, path);
	}

	@Override
	public Collection<IFSAState<E>> getFinalStates()
	{
		return Collections.unmodifiableCollection(finalStates);
	}

	@Override
	public Collection<IFSAState<E>> getInitialStates()
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
	public Collection<IFSAEdge<E>> getEdges(Collection<? extends IFSAState<E>> states)
	{
		List<IFSAEdge<E>> ret = new ArrayList<>();

		for (IFSAState<E> _state : states)
		{
			IGBuilderState<E> state = (IGBuilderState<E>) _state;
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

		for (IFSAEdge<E> edge : edges)
			s1.append(edge).append("\n");
		return s1.toString();
	}
}
