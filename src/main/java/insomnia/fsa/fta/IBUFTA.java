package insomnia.fsa.fta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import insomnia.data.INode;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;

/**
 * Bottom-up tree Automaton.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public interface IBUFTA<VAL, LBL> extends IFTA<VAL, LBL>
{
	IGFPA<VAL, LBL> getGFPA();

	/**
	 * Get all {@link IFTAEdge} that may validate parentStates.
	 * A naive implementation may return all the edges of the automaton.
	 * 
	 * @param parentStates
	 */
	Collection<IFTAEdge<VAL, LBL>> getHyperEdges(List<Collection<IFSAState<VAL, LBL>>> parentStates);

	// =========================================================================

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getInitials(IBUFTA<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		Collection<IFSAState<VAL, LBL>> states = new ArrayList<>(automaton.getGFPA().getInitialStates());

		Stream<IFSAState<VAL, LBL>> stream = states.stream().filter( //
			s -> !automaton.getGFPA().isRooted(s) //
				&& (!node.getValue().isPresent() || s.getValueCondition().test(node.getValue().get())) //
		);
		return stream.collect(Collectors.toList());
	}

}
