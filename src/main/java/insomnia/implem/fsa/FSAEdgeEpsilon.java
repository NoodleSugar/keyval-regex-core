package insomnia.implem.fsa;

import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;

/**
 * Edge for epsilon transition
 */
public class FSAEdgeEpsilon<VAL, LBL> extends AbstractFSAEdge<VAL, LBL> implements IFSALabelCondition<LBL>
{
	public FSAEdgeEpsilon(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child)
	{
		super(parent, child);
	}

	@Override
	public boolean test(LBL element)
	{
		return false;
	}

	@Override
	public boolean test()
	{
		return true;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(parent).append("-ε->").append(child);
		return buffer.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FSAEdgeEpsilon))
			return false;

		return super.equals(obj);
	}

	@Override
	public IFSALabelCondition<LBL> getLabelCondition()
	{
		return this;
	}
}
