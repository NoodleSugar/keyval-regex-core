package insomnia.fsa;

import java.util.List;
import java.util.Optional;

public interface IFSAElement<VAL, LBL>
{
	boolean isRooted();

	boolean isTerminal();

	List<LBL> getLabels();

	Optional<VAL> getValue();
}
