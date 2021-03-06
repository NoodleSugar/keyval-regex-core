package insomnia.implem.data;

import insomnia.data.IEdge;
import insomnia.data.INode;

public final class Edges
{
	private Edges()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class Edge<VAL, LBL> implements IEdge<VAL, LBL>
	{
		INode<VAL, LBL> parent;
		INode<VAL, LBL> child;

		LBL label;

		Edge(INode<VAL, LBL> parent, INode<VAL, LBL> child, LBL label)
		{
			this.parent = parent;
			this.child  = child;
			this.label  = label;
		}

		@Override
		public LBL getLabel()
		{
			return label;
		}

		@Override
		public void setLabel(LBL label)
		{
			this.label = label;
		}

		@Override
		public INode<VAL, LBL> getParent()
		{
			return parent;
		}

		@Override
		public INode<VAL, LBL> getChild()
		{
			return child;
		}

		@Override
		public String toString()
		{
			return IEdge.toString(this);
		}
	}

	// =========================================================================

	public static <VAL, LBL> IEdge<VAL, LBL> create(INode<VAL, LBL> parent, INode<VAL, LBL> child, LBL label)
	{
		return new Edge<>(parent, child, label);
	}
}
