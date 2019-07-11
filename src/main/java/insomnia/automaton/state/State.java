package insomnia.automaton.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import insomnia.automaton.edge.IEdge;

public class State implements IState<String>
{	
	private int id;
	private boolean isInitial;
	private boolean isFinal;
	private Collection<IEdge<String>> edges;
	
	public State()
	{
		id = -1;
		isInitial = false;
		isFinal = false;
		edges = new ArrayList<IEdge<String>>();
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public void setInitial(boolean isInitial)
	{
		this.isInitial = isInitial;
	}
	
	public void setFinal(boolean isFinal)
	{
		this.isFinal = isFinal;
	}
	
	@Override
	public int getId()
	{
		return id;
	}

	@Override
	public boolean isInitial()
	{
		return isInitial;
	}

	@Override
	public boolean isFinal()
	{
		return isFinal;
	}
	
	@Override
	public boolean add(IEdge<String> e)
	{
		return edges.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends IEdge<String>> c)
	{
		return edges.addAll(c);
	}

	@Override
	public void clear()
	{
		edges.clear();
	}

	@Override
	public boolean contains(Object o)
	{
		return edges.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return edges.containsAll(c);
	}

	@Override
	public boolean isEmpty()
	{
		return edges.isEmpty();
	}

	@Override
	public Iterator<IEdge<String>> iterator()
	{
		return edges.iterator();
	}

	@Override
	public boolean remove(Object o)
	{
		return edges.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return edges.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return edges.retainAll(c);
	}

	@Override
	public int size()
	{
		return edges.size();
	}

	@Override
	public Object[] toArray()
	{
		return edges.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return edges.toArray(a);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null)
			return false;
		if(o instanceof State)
		{
			State s = (State) o;
			if(s.getId() == id)
				return true;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		if(isFinal && isInitial)
			return "(" + id + "]";
		else if(isFinal)
			return "[" + id + "]";
		else if(isInitial)
			return "(" + id + ")";
		else
			return " " + id + " ";
	}
}
