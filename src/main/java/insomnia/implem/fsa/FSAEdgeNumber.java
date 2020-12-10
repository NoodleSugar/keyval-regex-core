package insomnia.implem.fsa;

import java.text.NumberFormat;
import java.text.ParseException;

import fsa.AbstractFSAEdge;
import fsa.IFSALabel;
import fsa.IFSAState;

public class FSAEdgeNumber<E, N extends Number> extends AbstractFSAEdge<E> implements IFSALabel<E>
{
	Number n;

	public FSAEdgeNumber(IFSAState<E> parent, IFSAState<E> child, N n)
	{
		super(parent, child);
		this.n = n;
	}

	@Override
	public boolean test(E element)
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
	public IFSALabel<E> getLabel()
	{
		return this;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FSAEdgeNumber))
			return false;

		@SuppressWarnings("unchecked")
		FSAEdgeNumber<E, N> edge = (FSAEdgeNumber<E, N>) obj;

		return super.equals(obj) && n.equals(edge.n);
	}

}
