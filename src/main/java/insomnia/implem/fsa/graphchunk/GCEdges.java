package insomnia.implem.fsa.graphchunk;

import java.security.InvalidParameterException;
import java.util.Optional;

import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVLabels;
import insomnia.implem.kv.data.KVValue;

public final class GCEdges
{
	private GCEdges()
	{

	}

	private static class GCEdge<LBL> implements IGCEdge<LBL>
	{
		private IFSALabelCondition<LBL> labelCondition;

		// =========================================================================

		public GCEdge(IFSALabelCondition<LBL> labelCondition)
		{
			this.labelCondition = labelCondition;
		}

		public IGCEdge<LBL> copy()
		{
			return new GCEdge<LBL>(labelCondition);
		}

		@Override
		public IFSALabelCondition<LBL> getLabelCondition()
		{
			return labelCondition;
		}

		@Override
		public Optional<String> getLabelAsString()
		{
			return Optional.empty();
		}

		@Override
		public String toString()
		{
			return labelCondition.toString();
		}
	}

	// =========================================================================

	public static <LBL> boolean isEpsilon(IGCEdge<LBL> edge)
	{
		return edge.getLabelCondition().test();
	}

	public static <LBL> boolean isAny(IGCEdge<LBL> edge)
	{
		return edge.getLabelCondition() == FSALabelConditions.trueCondition();
	}

	// =========================================================================

	public static <LBL> IGCEdge<LBL> copy(IGCEdge<LBL> src)
	{
		if (!(src instanceof GCEdge))
			throw new InvalidParameterException();

		return ((GCEdge<LBL>) src).copy();
	}

	public static IGCEdge<KVLabel> createFromKVValue(KVValue value)
	{
		switch (value.getType())
		{
		case NUMBER:
			return createNumber(value.getNumber());
		case STRING:
			return createEq(KVLabels.create(value.getString()));
		case NULL:
		default:
			return createEpsilon();
		}
	}

	public static <LBL> IGCEdge<LBL> createEpsilon()
	{
		return new GCEdge<>(FSALabelConditions.epsilonCondition());
	}

	public static <LBL> IGCEdge<LBL> createEq(LBL label)
	{
		return new GCEdge<LBL>(FSALabelConditions.createEq(label))
		{
			@Override
			public Optional<String> getLabelAsString()
			{
				return Optional.of(label.toString());
			}

			@Override
			public IGCEdge<LBL> copy()
			{
				return createEq(label);
			}
		};
	}

	public static <LBL> IGCEdge<LBL> createStringEq(String str)
	{
		return new GCEdge<LBL>(FSALabelConditions.createStrEq(str))
		{
			@Override
			public Optional<String> getLabelAsString()
			{
				return Optional.of(str);
			}

			@Override
			public IGCEdge<LBL> copy()
			{
				return createStringEq(str);
			}
		};
	}

	public static <LBL> IGCEdge<LBL> createRegex(String regex)
	{
		return new GCEdge<>(FSALabelConditions.createRegex(regex));
	}

	public static <LBL> IGCEdge<LBL> createNumber(Number nb)
	{
		return new GCEdge<>(FSALabelConditions.createStrEq(nb.toString()));
	}

	public static <LBL> IGCEdge<LBL> createAny()
	{
		return new GCEdge<>(FSALabelConditions.trueCondition());
	}
}