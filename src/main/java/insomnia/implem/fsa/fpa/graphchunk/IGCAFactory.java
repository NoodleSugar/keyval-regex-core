package insomnia.implem.fsa.fpa.graphchunk;

import java.util.Optional;

import insomnia.fsa.IFSAState;

/**
 * Abstract Factory for GraphChunk purposes.
 * 
 * @author zuri
 */
public interface IGCAFactory<VAL, LBL>
{
	IFSAState<VAL, LBL> create();

	IFSAState<VAL, LBL> create(Optional<VAL> value);

	void setInitial(IFSAState<VAL, LBL> state, boolean v);

	void setFinal(IFSAState<VAL, LBL> state, boolean v);

	void setRooted(IFSAState<VAL, LBL> state, boolean v);

	void setTerminal(IFSAState<VAL, LBL> state, boolean v);
}
