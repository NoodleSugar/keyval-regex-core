package insomnia.implem.fsa.state;

import insomnia.fsa.IFSAState;
import insomnia.fsa.IFSAValueCondition;

public final class FSAState<VAL, LBL> implements IFSAState<VAL, LBL>
{
	IFSAValueCondition<VAL> valueCondition;

	public FSAState(IFSAValueCondition<VAL> valueCondition)
	{
		this.valueCondition = valueCondition;
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
			.append(":").append(valueCondition);
		return sb.toString();
	}
}
