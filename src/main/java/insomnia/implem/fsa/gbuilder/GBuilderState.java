package insomnia.implem.fsa.gbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import insomnia.fsa.IFSAEdge;

public class GBuilderState<E> implements IGBuilderState<E>
{
	int id;

	List<IFSAEdge<E>> childs;

	public GBuilderState(int id)
	{
		this.id = id;
	}

	@Override
	public Collection<IGBuilderState<E>> getChilds()
	{
		Collection<IGBuilderState<E>> ret = new ArrayList<>(childs.size());

		for (IFSAEdge<E> edge : childs)
			ret.add((IGBuilderState<E>) edge.getChild());

		return ret;
	}

	@Override
	public Collection<IFSAEdge<E>> getEdges()
	{
		return childs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof GBuilderState))
			return false;

		return id == ((GBuilderState<E>) obj).id;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return "<" + id + ">";
	}
}
