package insomnia.fsa.algorithm;

import java.util.function.BiPredicate;

import insomnia.fsa.IFSAElement;
import insomnia.fsa.IFSAutomaton;

public interface IFSAValidation<VAL, LBL, AUTOMATA extends IFSAutomaton<VAL, LBL>, ELEMENT extends IFSAElement<VAL, LBL>> //
	extends BiPredicate<AUTOMATA, ELEMENT>
{
}
