package insomnia.fsa;

public interface IFSAProperties
{
	boolean isDeterministic();

	boolean isSynchronous();

	IFSAProperties setSynchronous(boolean b);

	IFSAProperties setDeterministic(boolean b);
}
