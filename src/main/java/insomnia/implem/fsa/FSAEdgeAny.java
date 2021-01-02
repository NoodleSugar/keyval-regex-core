package insomnia.implem.fsa;

import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;

/**
 * Edge for epsilon transition
 */
public class FSAEdgeAny<VAL, LBL> extends AbstractFSAEdge<VAL, LBL> implements IFSALabelCondition<LBL>
{
	public FSAEdgeAny(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child)
	{
		super(parent, child);
	}

	@Override
	public boolean test(LBL element)
	{
		return true;
	}

	@Override
	public boolean test()
	{
		return false;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(parent).append(" -(*)-> ").append(child);
		return buffer.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FSAEdgeAny))
			return false;

		return super.equals(obj);
	}

	@Override
	public IFSALabelCondition<LBL> getLabelCondition()
	{
		return this;
	}
}
