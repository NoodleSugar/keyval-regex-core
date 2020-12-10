package insomnia.implem.fsa;

import fsa.AbstractFSAEdge;
import fsa.IFSALabel;
import fsa.IFSAState;

/**
 * Edge for words
 */
public class FSAEdgeStringEq<E> extends AbstractFSAEdge<E> implements IFSALabel<E>
{
	String strCmp;

	public FSAEdgeStringEq(IFSAState<E> parent, IFSAState<E> child, String strCmp)
	{
		super(parent, child);
		this.strCmp = strCmp;
	}

	public String getWord()
	{
		return strCmp;
	}

	@Override
	public boolean test(E element)
	{
		return strCmp.equals(element.toString());
	}

	@Override
	public boolean test()
	{
		return false;
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(parent).append(" -[\"").append(strCmp).append("\"]-> ").append(child);
		return buffer.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FSAEdgeStringEq))
			return false;

		@SuppressWarnings("unchecked")
		FSAEdgeStringEq<E> edge = (FSAEdgeStringEq<E>) obj;

		return super.equals(obj) && strCmp.equals(edge.strCmp);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + strCmp.hashCode();
	}

	@Override
	public IFSALabel<E> getLabel()
	{
		return this;
	}
}
