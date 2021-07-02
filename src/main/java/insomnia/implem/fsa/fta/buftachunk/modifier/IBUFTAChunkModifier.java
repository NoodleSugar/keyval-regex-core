package insomnia.implem.fsa.fta.buftachunk.modifier;

import java.util.List;

import insomnia.data.ITree;
import insomnia.fsa.IFSALabelCondition;
import insomnia.fsa.IFSANodeCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;
import insomnia.implem.fsa.fta.edge.FTAEdge;

@FunctionalInterface
public interface IBUFTAChunkModifier<VAL, LBL>
{

	/**
	 * Data needed for the Modifier.
	 * 
	 * @author zuri
	 */
	public interface Environment<VAL, LBL>
	{
		BUFTAChunk<VAL, LBL> build(ITree<VAL, LBL> tree);

		/**
		 * Add a new simple edge to the automaton and create new state if needed (if <code>null</code> given)
		 * 
		 * @param automaton  the automaton
		 * @param parent     if <code>null</code> create a new state
		 * @param child      if <code>null</code> create a new state
		 * @param lcondition the label condition
		 * @return
		 */
		void addEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> parent, IFSAState<VAL, LBL> child, IFSALabelCondition<LBL> lcondition);

		IFTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> parent, IFSAState<VAL, LBL> state);

		FTAEdge<VAL, LBL> addChildFTAEdge(BUFTAChunk<VAL, LBL> automaton, IFSAState<VAL, LBL> state);

		IFSANodeCondition<VAL, LBL> createNodeCondition(boolean isRooted, boolean isTerminal);
	};

	void accept(BUFTAChunk<VAL, LBL> buftachunk, Environment<VAL, LBL> env);
}
