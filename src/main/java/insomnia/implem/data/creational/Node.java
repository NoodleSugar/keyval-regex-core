package insomnia.implem.data.creational;

import java.util.ArrayList;
import java.util.List;

import insomnia.data.IEdge;
import insomnia.data.INode;

/**
 * A node of a data.
 * 
 * @author zuri
 * @param <VAL> type of a node value
 * @param <LBL> type of an edge label
 */
class Node<VAL, LBL> implements INode<VAL, LBL>
{
	private VAL value;

	private Edge<VAL, LBL>        parent;
	private List<IEdge<VAL, LBL>> children;

	private boolean isRooted;
	private boolean isTerminal;

	Node()
	{
		children   = new ArrayList<IEdge<VAL, LBL>>();
		isRooted   = false;
		isTerminal = false;
	}

	Node(VAL val)
	{
		this();
		value = val;
	}

	List<IEdge<VAL, LBL>> getChildren()
	{
		return children;
	}

	Edge<VAL, LBL> getParent()
	{
		return parent;
	}

	void addEdge(int pos, Edge<VAL, LBL> edge)
	{
		children.add(pos, edge);
	}

	void addEdge(Edge<VAL, LBL> edge)
	{
		children.add(edge);
	}

	void removeEdge(int pos)
	{
		children.remove(pos);
	}

	void removeEdge(Edge<VAL, LBL> edge)
	{
		children.remove(edge);
	}

	void removeEdge(Iterable<? extends Edge<VAL, LBL>> edges)
	{
		for (var e : edges)
			children.remove(e);
	}

	void removeAllEdges()
	{
		children.clear();
	}

	void setParent(Edge<VAL, LBL> parent)
	{
		this.parent = parent;
	}

	public void setRooted(boolean isRooted)
	{
		this.isRooted = isRooted;
	}

	public void setTerminal(boolean isTerminal)
	{
		this.isTerminal = isTerminal;
	}

	@Override
	public void setValue(VAL value)
	{
		this.value = value;
	}

	@Override
	public Object getID()
	{
		return this;
	}

	@Override
	public VAL getValue()
	{
		return value;
	}

	@Override
	public boolean isRooted()
	{
		return isRooted;
	}

	@Override
	public boolean isTerminal()
	{
		return isTerminal;
	}

	@Override
	public String toString()
	{
		return INode.toString(this);
	}
}
