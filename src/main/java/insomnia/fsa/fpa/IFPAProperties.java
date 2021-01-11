package insomnia.fsa.fpa;

public interface IFPAProperties
{
	boolean isDeterministic();

	boolean isSynchronous();

	IFPAProperties setSynchronous(boolean b);

	IFPAProperties setDeterministic(boolean b);
}
