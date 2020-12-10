package insomnia.FSA.gbuilder;

import java.util.List;

import insomnia.FSA.IFSAEdge;
import insomnia.FSA.IFSAState;

public class GBuilderState<E> implements IFSAState<E>
{
	int               id;
	List<IFSAEdge<E>> childs;

	public GBuilderState(int id)
	{
		this.id = id;
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
