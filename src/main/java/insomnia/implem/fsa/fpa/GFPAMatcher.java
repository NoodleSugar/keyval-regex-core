package insomnia.implem.fsa.fpa;

import insomnia.data.IPath;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fpa.GFPAOp;
import insomnia.fsa.fpa.IGFPA;

public class GFPAMatcher<VAL, LBL> implements ITreeMatcher<VAL, LBL>
{
	private IGFPA<VAL, LBL> automaton;
	private IPath<VAL, LBL> element;

	public GFPAMatcher(IGFPA<VAL, LBL> automaton, IPath<VAL, LBL> element)
	{
		this.automaton = automaton;
		this.element   = element;
	}

	@Override
	public boolean matches()
	{
		return GFPAOp.test(automaton, element);
	}
}
