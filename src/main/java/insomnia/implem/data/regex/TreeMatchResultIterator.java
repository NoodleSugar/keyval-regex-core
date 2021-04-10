package insomnia.implem.data.regex;

import java.util.Iterator;

import insomnia.data.regex.ITreeMatchResult;
import insomnia.data.regex.ITreeMatcher;

public final class TreeMatchResultIterator<VAL, LBL> implements Iterator<ITreeMatchResult<VAL, LBL>>
{
	private final ITreeMatcher<VAL, LBL> matcher;

	public TreeMatchResultIterator(ITreeMatcher<VAL, LBL> matcher)
	{
		this.matcher = matcher;
	}

	@Override
	public boolean hasNext()
	{
		return matcher.find();
	}

	@Override
	public ITreeMatchResult<VAL, LBL> next()
	{
		return matcher.toMatchResult();
	}
}
