package insomnia.implem.kv.pregex.fsa;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAElement;
import insomnia.fsa.IFSAProperties;
import insomnia.fsa.IFSAState;
import insomnia.fsa.algorithm.IGFSAValidation;
import insomnia.implem.fsa.gbuilder.AbstractGBuilderFSA;
import insomnia.implem.fsa.gbuilder.GBuilder;
import insomnia.implem.fsa.gbuilder.GBuilderState;
import insomnia.implem.fsa.gbuilder.IGBuilderFSAFactory;
import insomnia.implem.fsa.graphchunk.GraphChunk;

/**
 * @author zuri
 * @param <V>
 * @param <E>
 * @param <T> Automaton type builded.
 */
class PRegexFSABuilder<VAL, LBL> extends GBuilder<VAL, LBL, GBuilderState<VAL, LBL>>
{
	public PRegexFSABuilder(GraphChunk<VAL, LBL> gc)
	{
		super(gc, state -> new GBuilderState<VAL, LBL>(state.getId(), state.getValueCondition()), new FSAFactory<VAL, LBL>());
	}

	static class FSAFactory<VAL, LBL> implements IGBuilderFSAFactory<VAL, LBL>
	{
		@Override
		public AbstractGBuilderFSA<VAL, LBL> get( //
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
			if (properties.isSynchronous())
				return new FSASync<VAL, LBL>(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validator);
			else
				return new FSAGeneral<VAL, LBL>(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validator);
		}
	}

	static abstract class AbstractFSA<VAL, LBL> extends AbstractGBuilderFSA<VAL, LBL>
	{
		public AbstractFSA( //
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
			super(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validator);
		}
	}

	private static class FSASync<VAL, LBL> extends AbstractFSA<VAL, LBL>
	{
		FSASync( //
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
			super(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, IFSAElement<VAL, LBL> element)
		{
			return nextValidState_sync(states, element);
		}
	}

	private static class FSAGeneral<VAL, LBL> extends AbstractFSA<VAL, LBL>
	{
		FSAGeneral( //
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
			super(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, IFSAElement<VAL, LBL> element)
		{
			return nextValidStates_general(states, element);
		}
	}
}