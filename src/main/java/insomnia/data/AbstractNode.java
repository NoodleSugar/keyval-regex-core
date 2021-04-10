package insomnia.data;

public abstract class AbstractNode<VAL, LBL> implements INode<VAL, LBL>
{
	@Override
	public abstract int hashCode();

	@Override
	public final boolean equals(Object obj)
	{
		return INode.sameAs(this, (INode<?, ?>) obj);
	}

	@Override
	public String toString()
	{
		return INode.toString(this);
	}
}
