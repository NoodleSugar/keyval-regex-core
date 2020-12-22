package insomnia.implem.kv.regex.automaton;

import java.util.Collection;
import java.util.List;

import insomnia.data.IPath;
import insomnia.data.ITree;
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

/**
 * @author zuri
 * @param <V>
 * @param <E>
 * @param <T> Automaton type builded.
 */
class RegexAutomatonBuilder<V, E> extends GBuilder<V, E, GBuilderState<E>>
{
	public RegexAutomatonBuilder(GraphChunk gc)
	{
		super(gc, id -> new GBuilderState<E>(id), new FSAFactory<V, E>());
	}

	static class FSAFactory<V, E> implements IGBuilderFSAFactory<E, ITree<V, E>>
	{
		@Override
		public AbstractGBuilderFSA<E, ITree<V, E>> get(Collection<IFSAState<E>> states, Collection<IFSAState<E>> initialStates, Collection<IFSAState<E>> finalStates, Collection<IFSAEdge<E>> edges, IFSAProperties properties, IFSAAValidation<ITree<V, E>, IGFSAutomaton<E, ITree<V, E>>> validator)
		{
			if (properties.isSynchronous())
				return new FSASync<V, E>(states, initialStates, finalStates, edges, properties, validator);
			else
				return new FSAGeneral<V, E>(states, initialStates, finalStates, edges, properties, validator);
		}
	}

	static class FSASync<V, E> extends AbstractGBuilderFSA<E, ITree<V, E>>
	{
		FSASync(Collection<IFSAState<E>> states, Collection<IFSAState<E>> initialStates, Collection<IFSAState<E>> finalStates, Collection<IFSAEdge<E>> edges, IFSAProperties properties, IFSAAValidation<ITree<V, E>, IGFSAutomaton<E, ITree<V, E>>> validator)
		{
			super(states, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, ITree<V, E> element)
		{
			return nextValidState_sync(states, element);
		}

		@Override
		public List<E> getLabelsOf(ITree<V, E> element)
		{
			return ((IPath<V, E>) element).getLabels();
		}
	}

	static class FSAGeneral<V, E> extends AbstractGBuilderFSA<E, ITree<V, E>>
	{
		FSAGeneral(Collection<IFSAState<E>> states, Collection<IFSAState<E>> initialStates, Collection<IFSAState<E>> finalStates, Collection<IFSAEdge<E>> edges, IFSAProperties properties, IFSAAValidation<ITree<V, E>, IGFSAutomaton<E, ITree<V, E>>> validator)
		{
			super(states, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<E>> nextValidStates(Collection<? extends IFSAState<E>> states, ITree<V, E> element)
		{
			return nextValidStates_general(states, element);
		}

		@Override
		public List<E> getLabelsOf(ITree<V, E> element)
		{
			return ((IPath<V, E>) element).getLabels();
		}
	}

}