package insomnia.fsa.algorithm;

import insomnia.fsa.IFSAElement;
import insomnia.fsa.IGFSAutomaton;

public interface IGFSAValidation<VAL, LBL> //
	extends IFSAValidation<VAL, LBL, IGFSAutomaton<VAL, LBL>, IFSAElement<VAL, LBL>>
{

	@Override
	boolean test(IGFSAutomaton<VAL, LBL> automaton, IFSAElement<VAL, LBL> element);
}
