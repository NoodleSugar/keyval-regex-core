package insomnia.implem.fsa.edge;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;

public final class FSAEdge<VAL, LBL> implements IFSAEdge<VAL, LBL>
{
	private IFSAState<VAL, LBL>     parent;
	private IFSAState<VAL, LBL>     child;
	private IFSALabelCondition<LBL> labelCondition;

	public FSAEdge(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child, IFSALabelCondition<LBL> labelCondition)
	{
		this.parent         = parent;
		this.child          = child;
		this.labelCondition = labelCondition;
	}

	@Override
	public IFSAState<VAL, LBL> getParent()
	{
		return parent;
	}

	@Override
	public IFSAState<VAL, LBL> getChild()
	{
		return child;
	}

	@Override
	public IFSALabelCondition<LBL> getLabelCondition()
	{
		return labelCondition;
	}

	// =========================================================================

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FSAEdge))
			return false;

		@SuppressWarnings("unchecked")
		FSAEdge<VAL, LBL> edge = (FSAEdge<VAL, LBL>) obj;

		return edge.getParent().equals(getParent()) //
			&& edge.getChild().equals(getChild()) //
			&& edge.getLabelCondition().equals(getLabelCondition());
	}

	@Override
	public int hashCode()
	{
		return getParent().hashCode() + getChild().hashCode() + labelCondition.hashCode();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(parent).append(" --");

		if (IFSAEdge.isEpsilon(this))
			sb.append("ε");
		else
			sb.append("(").append(labelCondition).append(")");

		sb.append("--> ").append(child);
		return sb.toString();
	}
}
