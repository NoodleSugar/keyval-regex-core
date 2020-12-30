package insomnia.implem.fsa;

import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.IFSALabel;
import insomnia.fsa.IFSAState;

/**
 * Edge for words
 */
public class FSAEdgeStringEq<VAL, LBL> extends AbstractFSAEdge<VAL, LBL> implements IFSALabel<LBL>
{
	String strCmp;

	public FSAEdgeStringEq(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child, String strCmp)
	{
		super(parent, child);
		this.strCmp = strCmp;
	}

	public String getWord()
	{
		return strCmp;
	}

	@Override
	public boolean test(LBL element)
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
		FSAEdgeStringEq<VAL, LBL> edge = (FSAEdgeStringEq<VAL, LBL>) obj;

		return super.equals(obj) && strCmp.equals(edge.strCmp);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + strCmp.hashCode();
	}

	@Override
	public IFSALabel<LBL> getLabel()
	{
		return this;
	}
}
