package insomnia.regex.automaton;

import java.util.ArrayDeque;
import java.util.Iterator;

public class Path extends ArrayDeque<String>
{
	private static final long serialVersionUID = -2561380984262758550L;
	
	public Path()
	{
		super();
	}
	
	public String getPath()
	{
		return String.join(".", this);
	}
	
	public Iterator<String> iterator()
	{
		return super.descendingIterator();
	}
	
	public Iterator<String> descendingIterator()
	{
		return super.iterator();
	}
	
	@Override
	public String toString()
	{
		return getPath();
	}
}