package insomnia.fsa.fpa;

import java.util.List;
import java.util.Optional;

public interface IFPAPath<VAL, LBL>
{
	boolean isRooted();

	boolean isTerminal();

	List<LBL> getLabels();

	Optional<VAL> getValue();
}
