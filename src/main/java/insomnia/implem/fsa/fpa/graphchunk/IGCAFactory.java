package insomnia.implem.fsa.fpa.graphchunk;

import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;

/**
 * Abstract Factory for GraphChunk purposes.
 * 
 * @author zuri
 */
public interface IGCAFactory<VAL, LBL>
{
	IFSAState<VAL, LBL> create();

	IFSAState<VAL, LBL> create(VAL value);

	IFSAState<VAL, LBL> create(IFSAValueCondition<VAL> valueCondition);

	void setInitial(IFSAState<VAL, LBL> state, boolean v);

	void setFinal(IFSAState<VAL, LBL> state, boolean v);

	void setRooted(IFSAState<VAL, LBL> state, boolean v);

	void setTerminal(IFSAState<VAL, LBL> state, boolean v);

}
