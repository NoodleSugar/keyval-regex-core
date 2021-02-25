package insomnia.fsa.fpa;

import insomnia.data.IPath;
import insomnia.data.regex.IPathMatcher;

/**
 * A Finite Path Automaton.
 */
public interface IFPA<VAL, LBL>
{
	IPathMatcher<VAL, LBL> matcher(IPath<VAL, LBL> element);
}
