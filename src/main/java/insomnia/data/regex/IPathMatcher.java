package insomnia.data.regex;

import java.util.regex.Matcher;

import insomnia.data.ITree;

/**
 * A Matcher of an internal {@link ITree}.
 * The interface shares the same ideas as the Java String {@link Matcher} one.
 * 
 * @author zuri
 * @see Matcher
 */
public interface IPathMatcher<VAL, LBL> extends ITreeMatcher<VAL, LBL>
{
	@Override
	IPathMatchResult<VAL, LBL> toMatchResult();
}
