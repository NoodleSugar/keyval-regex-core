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
public interface ITreeMatcher<VAL, LBL>
{
	/**
	 * Check if there is a match of the {@link ITree} element without computing a {@link ITreeMatchResult}.
	 */
	boolean matches();

	/**
	 * Move the state of the matcher to the next valid match.
	 * 
	 * @return {@code true} if there is a new match, else {@code false}
	 */
	boolean find();

	/**
	 * Get the last match as a {@link ITreeMatchResult}.
	 */
	ITreeMatchResult<VAL, LBL> toMatchResult();

	/**
	 * Get the last original match as a {@link ITreeMatchResult}.
	 * <em>Original</em> refer to the initial tree used for the matching.
	 */
	ITreeMatchResult<VAL, LBL> originalMatchResult();

	// =========================================================================

	public interface ITreeBothResults<VAL, LBL>
	{
		ITreeMatchResult<VAL, LBL> standard();

		ITreeMatchResult<VAL, LBL> original();
	}

	ITreeBothResults<VAL, LBL> bothResults();
}
