package insomnia.implem.FSA;

import insomnia.FSA.AbstractFSAEdge;
import insomnia.FSA.IFSALabel;
import insomnia.FSA.IFSAState;

/**
 * Edge for epsilon transition
 */
public class FSAEdgeEpsilon<E> extends AbstractFSAEdge<E> implements IFSALabel<E>
{
	public FSAEdgeEpsilon(IFSAState<E> parent, IFSAState<E> child)
	{
		super(parent, child);
	}

	@Override
	public boolean test(E element)
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
	public IFSALabel<E> getLabel()
	{
		return this;
	}
}
