package insomnia.fsa.fta;

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

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> getInitials(IGFPA<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		VAL                         value = node.getValue();
		Stream<IFSAState<VAL, LBL>> ret   = automaton.getInitialStates().stream();

		if (!node.isTerminal())
			ret = ret.filter(s -> !automaton.isTerminal(s));

		return automaton.getEpsilonClosure(ret.filter(s -> (IGFPA.testValue(s.getValueCondition(), value))).collect(Collectors.toList()));
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> internalGetInitials(IGFPA<VAL, LBL> automaton, INode<VAL, LBL> node)
	{
		VAL value = node.getValue();
		return automaton.getInitialStates().stream().filter( //
			s -> !automaton.isTerminal(s) //
				&& (IGFPA.testValue(s.getValueCondition(), value) //
				)).collect(Collectors.toList());
	}

	public static <VAL, LBL> Collection<IFSAState<VAL, LBL>> internalFilterNewStates(IGFPA<VAL, LBL> automaton, Collection<IFSAState<VAL, LBL>> newStates, boolean nodeIsRoot, boolean nodeIsRooted)
	{
		Stream<IFSAState<VAL, LBL>> ret = newStates.stream();

		if (nodeIsRoot)
		{
			if (!nodeIsRooted)
				ret = ret.filter(s -> !automaton.isRooted(s));
		}
		else
			ret = ret.filter(s -> !automaton.isRooted(s));

		return ret.collect(Collectors.toList());
	}
}