package insomnia.implem.fsa;

import fsa.IFSAProperties;

public class FSAProperties implements IFSAProperties
{
	boolean isDeterministic = true;
	boolean isSynchronous   = true;

	public FSAProperties(boolean isDeterministic, boolean isSync)
	{
		this.isDeterministic = isDeterministic;
		this.isSynchronous   = isSync;
	}

	public FSAProperties(IFSAProperties prop)
	{
		this(prop.isDeterministic(), prop.isSynchronous());
	}

	public static FSAProperties union(IFSAProperties a, IFSAProperties b)
	{
		return new FSAProperties( //
			a.isDeterministic() && b.isDeterministic(), //
			a.isSynchronous() && b.isSynchronous() //
		);
	}

	@Override
	public FSAProperties setDeterministic(boolean isDeterministic)
	{
		return new FSAProperties(isDeterministic, isSynchronous);
	}

	@Override
	public FSAProperties setSynchronous(boolean isSynchronous)
	{
		return new FSAProperties(isDeterministic, isSynchronous);
	}

	@Override
	public boolean isDeterministic()
	{
		return isDeterministic;
	}

	@Override
	public boolean isSynchronous()
	{
		return isSynchronous;
	}
}
