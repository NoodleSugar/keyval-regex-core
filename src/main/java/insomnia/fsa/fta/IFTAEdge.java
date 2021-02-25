package insomnia.fsa.fta;

import java.util.List;

import insomnia.fsa.IFSAState;

/**
 * An hyper-edge of the automaton that is an edge attempting to match a List of FSAState at input in the automaton matching process.
 * Even if it is a List of input states, the process can or cannot take into account the order on the states.
 * 
 * @author zuri
 * @param <VAL> Value type of a data node.
 * @param <LBL> Label type of a data edge.
 */
public interface IFTAEdge<VAL, LBL>
{
	List<IFSAState<VAL, LBL>> getParents();

	IFSAState<VAL, LBL> getChild();

	IFTAEdgeCondition<VAL, LBL> getCondition();
}
