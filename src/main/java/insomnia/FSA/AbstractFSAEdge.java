package insomnia.FSA;

public abstract class AbstractFSAEdge<E> implements IFSAEdge<E>
{
	protected IFSAState<E> parent;
	protected IFSAState<E> child;

	protected AbstractFSAEdge(IFSAState<E> parent, IFSAState<E> child)
	{
		this.parent = parent;
		this.child  = child;
	}

	@Override
	public IFSAState<E> getParent()
	{
		return parent;
	}

	@Override
	public IFSAState<E> getChild()
	{
		return child;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof AbstractFSAEdge))
			return false;

		@SuppressWarnings("unchecked")
		AbstractFSAEdge<E> edge = (AbstractFSAEdge<E>) obj;

		return edge.getParent().equals(getParent()) //
			&& edge.getChild().equals(getChild());
	}

	@Override
	public int hashCode()
	{
		return getParent().hashCode() + getChild().hashCode();
	}
}
