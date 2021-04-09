package insomnia.implem.data.regex;

import java.util.Iterator;

import insomnia.data.regex.ITreeMatchResult;
import insomnia.data.regex.ITreeMatcher;

public final class TreeMatchResultIterator<VAL, LBL> implements Iterator<ITreeMatchResult<VAL, LBL>>
{
	private final ITreeMatcher<VAL, LBL> matcher;

	private Mode mode;

	public enum Mode
	{
		STANDARD, ORIGINAL
	};

	public TreeMatchResultIterator(ITreeMatcher<VAL, LBL> matcher, Mode mode)
	{
		this.matcher = matcher;
		this.mode    = mode;
	}

	public TreeMatchResultIterator(ITreeMatcher<VAL, LBL> matcher)
	{
		this(matcher, Mode.STANDARD);
	}

	public void setMode(Mode mode)
	{
		this.mode = mode;
	}

	@Override
	public boolean hasNext()
	{
		return matcher.find();
	}

	public ITreeMatchResult<VAL, LBL> standard()
	{
		return matcher.toMatchResult();
	}

	public ITreeMatchResult<VAL, LBL> original()
	{
		return matcher.originalMatchResult();
	}

	@Override
	public ITreeMatchResult<VAL, LBL> next()
	{
		if (mode == Mode.STANDARD)
			return standard();
		else
			return original();
	}
}
