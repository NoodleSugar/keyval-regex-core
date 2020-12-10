package insomnia.implem.kv.regex.automaton;

import java.util.Collection;
import java.util.List;

import insomnia.FSA.IFSAEdge;
import insomnia.FSA.IFSAProperties;
import insomnia.FSA.IFSAState;
import insomnia.FSA.IGFSAutomaton;
import insomnia.FSA.algorithm.IFSAAValidation;
import insomnia.FSA.gbuilder.GBuilder;
import insomnia.FSA.gbuilder.GBuilderFSA;
import insomnia.FSA.gbuilder.GBuilderFSAFactory;
import insomnia.FSA.gbuilder.GBuilderState;
import insomnia.implem.FSA.GraphChunk;

class RegexAutomatonBuilder<E> extends GBuilder<E, GBuilderState<E>>
{
	public RegexAutomatonBuilder(GraphChunk gc)
	{
		super(gc, id -> new GBuilderState<E>(id), new FSAFactory<E>());
	}

	static class FSAFactory<E> implements GBuilderFSAFactory<E>
	{
		@Override
		public GBuilderFSA<E> get(Collection<IFSAState<E>> states, Collection<IFSAState<E>> initialStates, Collection<IFSAState<E>> finalStates, Collection<IFSAEdge<E>> edges, IFSAProperties properties, IFSAAValidation<E, IGFSAutomaton<E>> validator)
		{
			if (properties.isSynchronous())
				return new FSASync<E>(states, initialStates, finalStates, edges, properties, validator);
			else
				return new FSAGeneral<E>(states, initialStates, finalStates, edges, properties, validator);
		}
	}

	static class FSASync<E> extends GBuilderFSA<E>
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

	static class FSAGeneral<E> extends GBuilderFSA<E>
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