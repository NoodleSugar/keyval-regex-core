package insomnia.fsa;

import insomnia.lib.graph.IGraphEdge;

/**
 * @param
 * <LBL>        Label type.
 */
public interface IFSAEdge<VAL, LBL> extends IGraphEdge<IFSAState<VAL, LBL>>
{
	@Override
	IFSAState<VAL, LBL> getParent();

	@Override
	IFSAState<VAL, LBL> getChild();

	IFSALabelCondition<LBL> getLabelCondition();

	// =========================================================================

	static <VAL, LBL> boolean isEpsilon(IFSAEdge<VAL, LBL> edge)
	{
		return null == edge.getLabelCondition();
	}

}
