package insomnia.implem.fsa;

import insomnia.fsa.IFSAValueCondition;

class FSAValueAny<VAL> implements IFSAValueCondition<VAL>
{
	@Override
	public boolean test(VAL element)
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "*";
	}
}
