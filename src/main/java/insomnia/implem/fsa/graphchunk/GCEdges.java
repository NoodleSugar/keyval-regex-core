package insomnia.implem.fsa.graphchunk;

import java.security.InvalidParameterException;
import java.util.Optional;

import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.labelcondition.FSALabelConditions;

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

	public static <LBL> IGCEdge<LBL> createRegex(String regex)
	{
		return new GCEdge<>(FSALabelConditions.createRegex(regex));
	}

	public static <LBL> IGCEdge<LBL> createAny()
	{
		return new GCEdge<>(FSALabelConditions.trueCondition());
	}
}