package insomnia.implem.fsa;

import java.util.regex.Pattern;

import insomnia.fsa.AbstractFSAEdge;
import insomnia.fsa.IFSALabel;
import insomnia.fsa.IFSAState;

/**
 * Edge for regex
 */
public class FSAEdgeRegex<VAL, LBL> extends AbstractFSAEdge<VAL, LBL> implements IFSALabel<LBL>
{
	Pattern pattern;

	public FSAEdgeRegex(IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child, String regex)
	{
		super(parent, child);
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean test(LBL element)
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
		FSAEdgeRegex<VAL, LBL> edge = (FSAEdgeRegex<VAL, LBL>) obj;

		return pattern.toString().equals(edge.pattern.toString());
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + pattern.toString().hashCode();
	}

	@Override
	public IFSALabel<LBL> getLabel()
	{
		return this;
	}
}
