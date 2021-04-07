package insomnia.implem.fsa.state;

import insomnia.fsa.IFSANodeCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;

public final class FSAState<VAL, LBL> implements IFSAState<VAL, LBL>
{
	IFSAValueCondition<VAL> valueCondition;

	IFSANodeCondition<VAL, LBL> nodeCondition;

	public FSAState(IFSAValueCondition<VAL> valueCondition, IFSANodeCondition<VAL, LBL> nodeCondition)
	{
		this.valueCondition = valueCondition;
		this.nodeCondition  = nodeCondition;
	}

	@Override
	public IFSANodeCondition<VAL, LBL> getNodeCondition()
	{
		return nodeCondition;
	}

	@Override
	public IFSAValueCondition<VAL> getValueCondition()
	{
		return valueCondition;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toHexString(System.identityHashCode(this))) //
			.append(":").append(valueCondition) //
			.append(";N:").append(valueCondition) //
		;
		return sb.toString();
	}
}
