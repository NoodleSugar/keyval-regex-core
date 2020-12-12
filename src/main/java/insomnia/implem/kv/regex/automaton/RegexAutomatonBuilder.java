package insomnia.implem.kv.regex.automaton;

import java.util.Collection;
import java.util.List;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAProperties;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IGFSAutomaton;
import insomnia.fsa.algorithm.IFSAAValidation;
import insomnia.fsa.gbuilder.AbstractGBuilderFSA;
import insomnia.fsa.gbuilder.IGBuilderFSAFactory;
import insomnia.implem.fsa.gbuilder.GBuilder;
import insomnia.implem.fsa.gbuilder.GBuilderState;
import insomnia.implem.fsa.gbuilder.GraphChunk;

class RegexAutomatonBuilder<E> extends GBuilder<E, GBuilderState<E>>
{
	public RegexAutomatonBuilder(GraphChunk gc)
	{
		super(gc, id -> new GBuilderState<E>(id), new FSAFactory<E>());
	}

	static class FSAFactory<E> implements IGBuilderFSAFactory<E>
	{
		@Override
		public AbstractGBuilderFSA<E> get(Collection<IFSAState<E>> states, Collection<IFSAState<E>> initialStates, Collection<IFSAState<E>> finalStates, Collection<IFSAEdge<E>> edges, IFSAProperties properties, IFSAAValidation<E, IGFSAutomaton<E>> validator)
		{
			if (properties.isSynchronous())
				return new FSASync<E>(states, initialStates, finalStates, edges, properties, validator);
			else
				return new FSAGeneral<E>(states, initialStates, finalStates, edges, properties, validator);
		}
	}

	static class FSASync<E> extends AbstractGBuilderFSA<E>
	{
		FSASync(Collection<IFSAState<E>> states, Collection<IFSAState<E>> initialStates, Collection<IFSAState<E>> finalStates, Collection<IFSAEdge<E>> edges, IFSAProperties properties, IFSAAValidation<E, IGFSAutomaton<E>> validator)
		{
			super(states, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, List<E> elements)
		{
			return nextValidState_sync(states, elements);
		}
	}

	static class FSAGeneral<E> extends AbstractGBuilderFSA<E>
	{
		FSAGeneral(Collection<IFSAState<E>> states, Collection<IFSAState<E>> initialStates, Collection<IFSAState<E>> finalStates, Collection<IFSAEdge<E>> edges, IFSAProperties properties, IFSAAValidation<E, IGFSAutomaton<E>> validator)
		{
			super(states, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, List<E> elements)
		{
			return nextValidStates_general(states, elements);
		}
	}

}