package insomnia.implem.fsa.fpa.gbuilder;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public interface IGBuilderState<VAL, LBL> extends IFSAState<VAL, LBL>
{
	Collection<IFSAEdge<VAL, LBL>> getEdges();
}
