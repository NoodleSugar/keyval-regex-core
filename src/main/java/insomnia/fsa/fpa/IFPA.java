package insomnia.fsa.fpa;

import insomnia.data.IPath;
import insomnia.data.regex.ITreeMatcher;

/**
 * A Finite Path Automaton.
 */
public interface IFPA<VAL, LBL>
{
	ITreeMatcher<VAL, LBL> matcher(IPath<VAL, LBL> element);
}
