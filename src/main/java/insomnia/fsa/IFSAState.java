package insomnia.fsa;

public interface IFSAState<VAL, LBL>
{
	IFSAValueCondition<VAL> getValueCondition();
}
