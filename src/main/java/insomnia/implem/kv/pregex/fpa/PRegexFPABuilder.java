package insomnia.implem.kv.pregex.fpa;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IFPAPath;
import insomnia.fsa.fpa.IFPAProperties;
import insomnia.fsa.fpa.algorithm.IGFPAValidation;
import insomnia.fsa.fpa.factory.AbstractGFPABuilder;
import insomnia.implem.fsa.fpa.gbuilder.AbstractGBuilderFPA;
import insomnia.implem.fsa.fpa.gbuilder.GBuilder;
import insomnia.implem.fsa.fpa.gbuilder.GBuilderState;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;

/**
 * @author zuri
 * @param <V>
 * @param <E>
 * @param <T> Automaton type builded.
 */
class PRegexFPABuilder<VAL, LBL> extends GBuilder<VAL, LBL, GBuilderState<VAL, LBL>>
{
	public PRegexFPABuilder(GraphChunk<VAL, LBL> gc)
	{
		super(gc, state -> new GBuilderState<VAL, LBL>(state.getId(), state.getValueCondition()), new FSAFactory<VAL, LBL>());
	}

	static class FSAFactory<VAL, LBL> extends AbstractGFPABuilder<VAL, LBL, AbstractGBuilderFPA<VAL, LBL>>
	{
		@Override
		public AbstractGBuilderFPA<VAL, LBL> create()
		{
			if (properties.isSynchronous())
				return new FSASync<VAL, LBL>(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validation);
			else
				return new FSAGeneral<VAL, LBL>(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validation);
		}
	}

	static abstract class AbstractFSA<VAL, LBL> extends AbstractGBuilderFPA<VAL, LBL>
	{
		public AbstractFSA( //
			Collection<IFSAState<VAL, LBL>> states, //
			Collection<IFSAState<VAL, LBL>> rootedStates, //
			Collection<IFSAState<VAL, LBL>> terminalStates, //
			Collection<IFSAState<VAL, LBL>> initialStates, //
			Collection<IFSAState<VAL, LBL>> finalStates, //
			Collection<IFSAEdge<VAL, LBL>> edges, //
			IFPAProperties properties, //
			IGFPAValidation<VAL, LBL> validator //
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
			IFPAProperties properties, //
			IGFPAValidation<VAL, LBL> validator //
		)
		{
			super(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, IFPAPath<VAL, LBL> element)
		{
			return GFPAOp.nextValidState_sync(this, states, element);
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
			IFPAProperties properties, //
			IGFPAValidation<VAL, LBL> validator //
		)
		{
			super(states, rootedStates, terminalStates, initialStates, finalStates, edges, properties, validator);
		}

		@Override
		public Collection<IFSAState<VAL, LBL>> nextValidStates(Collection<? extends IFSAState<VAL, LBL>> states, IFPAPath<VAL, LBL> element)
		{
			return GFPAOp.nextValidStates_general(this, states, element);
		}
	}
}