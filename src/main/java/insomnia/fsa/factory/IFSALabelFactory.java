package insomnia.fsa.factory;

/**
 * Factory of LBL needed in some FSA processes.
 * 
 * @author zuri
 */
public interface IFSALabelFactory<LBL>
{
	LBL create(String label);
}
