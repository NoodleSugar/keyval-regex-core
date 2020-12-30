package insomnia.fsa;

public abstract class AbstractFSAEdge<VAL, LBL> implements IFSAEdge<VAL, LBL>
{
	protected IFSAState<VAL, LBL> parent;
	protected IFSAState<VAL, LBL> child;

	protected AbstractFSAEdge(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child)
	{
		this.parent = parent;
		this.child  = child;
	}

	@Override
	public IFSAState<VAL, LBL> getParent()
	{
		return parent;
	}

	@Override
	public IFSAState<VAL, LBL> getChild()
	{
		return child;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof AbstractFSAEdge))
			return false;

		@SuppressWarnings("unchecked")
		AbstractFSAEdge<VAL, LBL> edge = (AbstractFSAEdge<VAL, LBL>) obj;

		return edge.getParent().equals(getParent()) //
			&& edge.getChild().equals(getChild());
	}

	@Override
	public int hashCode()
	{
		return getParent().hashCode() + getChild().hashCode();
	}
}
