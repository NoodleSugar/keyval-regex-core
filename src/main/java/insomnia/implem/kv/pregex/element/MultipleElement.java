package insomnia.implem.kv.pregex.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Concatenation element
 */
public class MultipleElement extends AbstractElement implements Collection<IElement>
{
	ArrayList<IElement> elements;

	public MultipleElement()
	{
		super();
		elements = new ArrayList<IElement>();
	}

	@Override
	public boolean add(IElement e)
	{
		return elements.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends IElement> c)
	{
		return elements.addAll(c);
	}

	@Override
	public void clear()
	{
		elements.clear();
	}

	@Override
	public boolean contains(Object o)
	{
		return elements.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return elements.containsAll(c);
	}

	@Override
	public boolean isEmpty()
	{
		return elements.isEmpty();
	}

	@Override
	public Iterator<IElement> iterator()
	{
		return elements.iterator();
	}

	@Override
	public boolean remove(Object o)
	{
		return elements.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return elements.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return elements.retainAll(c);
	}

	@Override
	public int size()
	{
		return elements.size();
	}

	@Override
	public Object[] toArray()
	{
		return elements.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return elements.toArray(a);
	}
	
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		boolean hasQuantifier = quantifier.getInf() != 1 || quantifier.getSup() != 1;
		
		if(hasQuantifier)
			s.append('(');
		
		boolean first = true;
		for(IElement e : this)
		{
			if(first)
				first = false;
			else
				s.append('.');
			if(e instanceof Key)
				s.append(e);
			else
				s.append('(').append(e).append(')');
		}
		if(hasQuantifier)
			s.append(')').append(quantifier);
		
		return s.toString();
	}
}