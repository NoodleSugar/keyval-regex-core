package insomnia.implem.data.creational;

import java.util.Collection;

import insomnia.data.IEdge;

/**
 * An edge of a data.
 * 
 * @author zuri
 * @param <VAL> type of a node value
 * @param <LBL> type of an edge label
 */
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
	public Node<VAL, LBL> getParent()
	{
		return parent;
	}

	@Override
	public Node<VAL, LBL> getChild()
	{
		return child;
	}

	@Override
	public String toString()
	{
		return IEdge.toString(this);
	}

	// =========================================================================

	/**
	 * Change the parent of the {@link Edge}.
	 * 
	 * @param newParent the new parent node
	 */
	void setParent(Node<VAL, LBL> newParent)
	{
		parent.removeEdge(this);
		newParent.addEdge(this);
		parent = newParent;
	}

	/**
	 * Change the child of the {@link Edge}.
	 * 
	 * @param newParent the new child node
	 */
	void setChild(Node<VAL, LBL> newChild)
	{
		child.setParent(null);
		newChild.setParent(this);
		child = newChild;
	}
}
