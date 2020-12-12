package fsa.gbuilder;

import java.util.Collection;

import fsa.IFSAEdge;
import fsa.IFSAState;

public interface IGBuilderState<E> extends IFSAState<E>
{
	Collection<IGBuilderState<E>> getChilds();

	Collection<IFSAEdge<E>> getEdges();
}
