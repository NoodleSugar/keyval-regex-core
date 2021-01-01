package insomnia.implem.fsa;

import java.text.NumberFormat;
import java.text.ParseException;

import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSAState;

public class FSAEdgeNumber<VAL, LBL, N extends Number> extends AbstractFSAEdge<VAL, LBL> implements IFSALabelCondition<LBL>
{
	Number n;

	public FSAEdgeNumber(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child, N n)
	{
		super(parent, child);
		this.n = n;
	}

	@Override
	public boolean test(LBL element)
	{
		try
		{
			return n.equals(NumberFormat.getInstance().parse(element.toString()));
		}
		catch (ParseException e)
		{
			return false;
		}
	}

	@Override
	public boolean test()
	{
		return false;
	}

	@Override
	public IFSALabelCondition<LBL> getLabelCondition()
	{
		return this;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FSAEdgeNumber))
			return false;

		@SuppressWarnings("unchecked")
		FSAEdgeNumber<VAL, LBL, N> edge = (FSAEdgeNumber<VAL, LBL, N>) obj;

		return super.equals(obj) && n.equals(edge.n);
	}

}
