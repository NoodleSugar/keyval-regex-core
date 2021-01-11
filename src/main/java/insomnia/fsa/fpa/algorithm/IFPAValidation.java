package insomnia.fsa.fpa.algorithm;

import java.util.function.BiPredicate;

import insomnia.fsa.fpa.IFPAPath;
import insomnia.fsa.fpa.IFPA;

public interface IFPAValidation<VAL, LBL, AUTOMATA extends IFPA<VAL, LBL>, ELEMENT extends IFPAPath<VAL, LBL>> //
	extends BiPredicate<AUTOMATA, ELEMENT>
{
}
