package insomnia.implem.fsa.fpa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAValueCondition;

public class GBuilderState<VAL, LBL> implements IGBuilderState<VAL, LBL>
{
	int id;

	List<IFSAEdge<VAL, LBL>> childs;

	IFSAValueCondition<VAL> valueCond;

	public GBuilderState(int id, IFSAValueCondition<VAL> valueCondition)
	{
		this.id        = id;
		this.valueCond = valueCondition;
	}

	@Override
	public Collection<IGBuilderState<VAL, LBL>> getChilds()
	{
		Collection<IGBuilderState<VAL, LBL>> ret = new ArrayList<>(childs.size());

		for (IFSAEdge<VAL, LBL> edge : childs)
			ret.add((IGBuilderState<VAL, LBL>) edge.getChild());

		return ret;
	}

	@Override
	public Collection<IFSAEdge<VAL, LBL>> getEdges()
	{
		return childs;
	}

	@Override
	public IFSAValueCondition<VAL> getValueCondition()
	{
		return valueCond;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof GBuilderState))
			return false;

		return id == ((GBuilderState<VAL, LBL>) obj).id;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return "<" + id + valueCond + ">";
	}
}
