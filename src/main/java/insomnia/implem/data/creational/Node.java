package insomnia.implem.data.creational;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import insomnia.data.IEdge;
import insomnia.data.INode;

class Node<VAL, LBL> implements INode<VAL, LBL>
{
	private Optional<VAL> value;

	private Edge<VAL, LBL>        parent   = null;
	private List<IEdge<VAL, LBL>> children = new ArrayList<>();

	private boolean isRooted   = false;
	private boolean isTerminal = false;

	Node()
	{
		this.value = Optional.empty();
	}

	List<IEdge<VAL, LBL>> getChildren()
	{
		return children;
	}

	Edge<VAL, LBL> getParent()
	{
		return parent;
	}

	void addEdge(Edge<VAL, LBL> edge)
	{
		children.add(edge);
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

	void setValue(VAL value)
	{
		this.value      = Optional.of(value);
		this.isTerminal = true;
	}

	@Override
	public Optional<VAL> getValue()
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
}
