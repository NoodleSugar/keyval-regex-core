package insomnia.fsa;

/**
 * @param
 * 	   <LBL> Label type.
 */
public interface IFSAEdge<VAL, LBL>
{
	IFSAState<VAL, LBL> getParent();

	IFSAState<VAL, LBL> getChild();

	IFSALabelCondition<LBL> getLabelCondition();

	// =========================================================================

	static <VAL, LBL> boolean isEpsilon(IFSAEdge<VAL, LBL> edge)
	{
		return null == edge.getLabelCondition();
	}
}
