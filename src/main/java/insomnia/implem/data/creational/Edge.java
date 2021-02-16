package insomnia.implem.data.creational;

import java.util.Collection;

import insomnia.data.IEdge;
import insomnia.data.INode;

class Edge<VAL, LBL> implements IEdge<VAL, LBL>
{
	private LBL            label;
	private Node<VAL, LBL> parent;
	private Node<VAL, LBL> child;

	Edge(LBL label, Node<VAL, LBL> parent, Node<VAL, LBL> child, Collection<LBL> vocabulary)
	{
		this.label  = label;
		this.parent = parent;
		this.child  = child;
		vocabulary.add(label);
		parent.addEdge(this);
		child.setParent(this);
	}

	@Override
	public LBL getLabel()
	{
		return label;
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
