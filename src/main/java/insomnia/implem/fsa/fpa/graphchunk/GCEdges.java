package insomnia.implem.fsa.fpa.graphchunk;

import java.security.InvalidParameterException;
import java.util.Optional;

import insomnia.fsa.IFSALabelCondition;

final class GCEdges
{
	private GCEdges()
	{
		throw new AssertionError();
	}

	// =========================================================================

	static class GCEdge<VAL, LBL> implements IGCEdge<VAL, LBL>
	{
		IFSALabelCondition<LBL> labelCondition;

		public GCEdge(IFSALabelCondition<LBL> labelCondition)
		{
			this.labelCondition = labelCondition;
		}

		public IGCEdge<VAL, LBL> copy()
		{
			return new GCEdge<VAL, LBL>(labelCondition);
		}

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

	public static <VAL, LBL> IGCEdge<VAL, LBL> copy(IGCEdge<VAL, LBL> src)
	{
		if (!(src instanceof GCEdge))
			throw new InvalidParameterException();

		return ((GCEdge<VAL, LBL>) src).copy();
	}
}