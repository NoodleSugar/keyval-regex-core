package insomnia.fsa.gbuilder;

import java.util.Collection;

import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;

public interface IGBuilderState<E> extends IFSAState<E>
{
	Collection<IGBuilderState<E>> getChilds();

	Collection<IFSAEdge<E>> getEdges();
}
