package insomnia.implem.fsa;

import insomnia.fsa.IFSAValueCondition;

class FSAValueEq<VAL> implements IFSAValueCondition<VAL>
{
	VAL value;

	public FSAValueEq(VAL value)
	{
		this.value = value;
	}

	@Override
	public boolean test(VAL element)
	{
		return value.equals(element);
	}

	@Override
	public String toString()
	{
		return "=" + value.toString();
	}
}
