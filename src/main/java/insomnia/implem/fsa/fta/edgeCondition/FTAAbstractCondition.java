package insomnia.implem.fsa.fta.edgeCondition;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import insomnia.fsa.IFSAState;
import insomnia.fsa.fta.IBUFTA;
import insomnia.fsa.fta.IFTAEdge;
import insomnia.fsa.fta.IFTAEdgeCondition;

abstract class FTAAbstractCondition<VAL, LBL> implements IFTAEdgeCondition<VAL, LBL>
{
	protected IBUFTA<VAL, LBL> automaton;

	protected List<IFSAState<VAL, LBL>> parentStates;

	public FTAAbstractCondition(IBUFTA<VAL, LBL> automaton, IFTAEdge<VAL, LBL> ftaEdge)
	{
		this.automaton = automaton;
		parentStates   = ftaEdge.getParents();
	}

	public List<IFSAState<VAL, LBL>> getParentStates()
	{
		return parentStates;
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
}
