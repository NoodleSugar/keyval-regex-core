package insomnia.implem.fsa.fta.edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.fsa.fta.IFTAEdgeCondition;

public final class FTAEdge<VAL, LBL> implements IFTAEdge<VAL, LBL>
{
	private List<IFSAState<VAL, LBL>>   parents;
	private IFSAState<VAL, LBL>         child;
	private IFTAEdgeCondition<VAL, LBL> condition;

	public FTAEdge(List<IFSAState<VAL, LBL>> parents, IFSAState<VAL, LBL> child, IFTAEdgeCondition<VAL, LBL> condition)
	{
		this.parents   = Collections.unmodifiableList(new ArrayList<>(parents));
		this.child     = child;
		this.condition = condition;
	}

	@Override
	public List<IFSAState<VAL, LBL>> getParents()
	{
		return parents;
	}

	@Override
	public IFSAState<VAL, LBL> getChild()
	{
		return child;
	}

	@Override
	public IFTAEdgeCondition<VAL, LBL> getCondition()
	{
		return condition;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(parents).append(" ").append(" -(").append(condition).append(")-> ").append(child);
		return sb.toString();
	}
}
