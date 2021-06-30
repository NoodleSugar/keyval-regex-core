package insomnia.implem.fsa.fta.edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IFTAEdge;

public final class FTAEdge<VAL, LBL> implements IFTAEdge<VAL, LBL>
{
	private List<IFSAState<VAL, LBL>>  parents;
	private IFSAState<VAL, LBL>        child;
	private ConditionFactory<VAL, LBL> conditionFactory;

	public FTAEdge(List<IFSAState<VAL, LBL>> parents, IFSAState<VAL, LBL> child, ConditionFactory<VAL, LBL> conditionFactory)
	{
		this.parents          = Collections.unmodifiableList(new ArrayList<>(parents));
		this.child            = child;
		this.conditionFactory = conditionFactory;
	}

	@Override
	public ConditionFactory<VAL, LBL> getConditionFactory()
	{
		return conditionFactory;
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
	public int hashCode()
	{
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	@Override
	public boolean equals(Object obj)
	{
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(parents).append(" ").append(" -(").append(conditionFactory).append(")-> ").append(child);
		return sb.toString();
	}
}
