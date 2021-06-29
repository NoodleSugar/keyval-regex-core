package insomnia.implem.fsa.fta.buftachunk.modifier;

import java.util.List;

import insomnia.data.ITree;
import insomnia.fsa.IFSANodeCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;

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

		IFTAEdge<VAL, LBL> addFTAEdge(BUFTAChunk<VAL, LBL> automaton, List<IFSAState<VAL, LBL>> parent, IFSAState<VAL, LBL> state);

		IFSANodeCondition<VAL, LBL> createNodeCondition(boolean isRooted, boolean isTerminal);
	};

	void accept(BUFTAChunk<VAL, LBL> buftachunk, Environment<VAL, LBL> env);
}
