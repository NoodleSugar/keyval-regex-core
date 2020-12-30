package insomnia.implem.fsa.gbuilder;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public interface IGBuilderState<VAL, LBL> extends IFSAState<VAL, LBL>
{
	Collection<IGBuilderState<VAL, LBL>> getChilds();

	Collection<IFSAEdge<VAL, LBL>> getEdges();
}
