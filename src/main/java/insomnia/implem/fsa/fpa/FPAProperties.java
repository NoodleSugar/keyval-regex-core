package insomnia.implem.fsa.fpa;

import insomnia.fsa.fpa.IFPAProperties;

public class FPAProperties implements IFPAProperties
{
	boolean isDeterministic = true;
	boolean isSynchronous   = true;

	public FPAProperties(boolean isDeterministic, boolean isSync)
	{
		this.isDeterministic = isDeterministic;
		this.isSynchronous   = isSync;
	}

	public FPAProperties(IFPAProperties prop)
	{
		this(prop.isDeterministic(), prop.isSynchronous());
	}

	public static FPAProperties union(IFPAProperties a, IFPAProperties b)
	{
		return new FPAProperties( //
			a.isDeterministic() && b.isDeterministic(), //
			a.isSynchronous() && b.isSynchronous() //
		);
	}

	@Override
	public FPAProperties setDeterministic(boolean isDeterministic)
	{
		return new FPAProperties(isDeterministic, isSynchronous);
	}

	@Override
	public FPAProperties setSynchronous(boolean isSynchronous)
	{
		return new FPAProperties(isDeterministic, isSynchronous);
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
