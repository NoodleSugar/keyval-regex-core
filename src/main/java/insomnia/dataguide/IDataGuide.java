package insomnia.dataguide;

import insomnia.data.ITree;
import insomnia.implem.fsa.fta.buftachunk.modifier.IBUFTAChunkModifier;

public interface IDataGuide<VAL, LBL>
{
	public enum NodeType
	{
		ARRAY, OBJECT
	};

	IBUFTAChunkModifier<VAL, LBL> getModifier();

	void addTree(ITree<VAL, LBL> tree);

	// =========================================================================

}
