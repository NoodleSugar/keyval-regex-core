package insomnia.fsa.fpa.algorithm;

import insomnia.fsa.fpa.IFPAPath;
import insomnia.fsa.fpa.IGFPA;

public interface IGFPAValidation<VAL, LBL> //
	extends IFPAValidation<VAL, LBL, IGFPA<VAL, LBL>, IFPAPath<VAL, LBL>>
{

	@Override
	boolean test(IGFPA<VAL, LBL> automaton, IFPAPath<VAL, LBL> element);
}
