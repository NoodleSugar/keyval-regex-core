package insomnia.implem.data.regex;

import java.util.Iterator;

import insomnia.data.regex.ITreeMatcher;
import insomnia.data.regex.ITreeMatcher.ITreeBothResults;

public final class TreeBothMatchResultIterator<VAL, LBL> implements Iterator<ITreeMatcher.ITreeBothResults<VAL, LBL>>
{
	private final ITreeMatcher<VAL, LBL> matcher;

	public TreeBothMatchResultIterator(ITreeMatcher<VAL, LBL> matcher)
	{
		this.matcher = matcher;
	}

	@Override
	public boolean hasNext()
	{
		return matcher.find();
	}

	@Override
	public ITreeBothResults<VAL, LBL> next()
	{
		return matcher.bothResults();
	}
}
