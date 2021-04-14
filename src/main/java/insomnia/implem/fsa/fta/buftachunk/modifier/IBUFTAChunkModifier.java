package insomnia.implem.fsa.fta.buftachunk.modifier;

import insomnia.data.ITree;
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
	};

	void accept(BUFTAChunk<VAL, LBL> buftachunk, Environment<VAL, LBL> env);
}
