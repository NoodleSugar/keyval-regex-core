package insomnia.fsa.factory;

public interface IFSALabelFactory<LBL>
{
	LBL create(String label);
}
