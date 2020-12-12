package insomnia.implem.fsa;

import java.util.regex.Pattern;

import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.IFSALabel;
import insomnia.fsa.IFSAState;

/**
 * Edge for regex
 */
public class FSAEdgeRegex<E> extends AbstractFSAEdge<E> implements IFSALabel<E>
{
	Pattern pattern;

	public FSAEdgeRegex(IFSAState<E> parent, IFSAState<E> child, String regex)
	{
		super(parent, child);
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean test(E element)
	{
		return pattern.matcher(element.toString()).matches();
	}

	@Override
	public boolean test()
	{
		return false;
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(parent).append(" -/").append(pattern.toString()).append("/-> ").append(child);
		return buffer.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FSAEdgeStringEq))
			return false;

		@SuppressWarnings("unchecked")
		FSAEdgeRegex<E> edge = (FSAEdgeRegex<E>) obj;

		return pattern.toString().equals(edge.pattern.toString());
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + pattern.toString().hashCode();
	}

	@Override
	public IFSALabel<E> getLabel()
	{
		return this;
	}
}
