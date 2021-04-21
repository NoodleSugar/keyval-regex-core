package insomnia.implem.fsa.fta.edgeCondition;

import java.util.List;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdgeCondition;

abstract class FTAAbstractCondition<VAL, LBL> implements IFTAEdgeCondition<VAL, LBL>
{
	protected List<IFSAState<VAL, LBL>> parentStates;

	public FTAAbstractCondition(List<IFSAState<VAL, LBL>> states)
	{
		setParentStates(states);
	}

	public void setParentStates(List<IFSAState<VAL, LBL>> parentStates)
	{
		this.parentStates = List.copyOf(parentStates);
	}

	public List<IFSAState<VAL, LBL>> getParentStates()
	{
		return parentStates;
	}
}
