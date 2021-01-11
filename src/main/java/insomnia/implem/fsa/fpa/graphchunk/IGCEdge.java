package insomnia.implem.fsa.fpa.graphchunk;

import java.util.Optional;

import insomnia.fsa.IFSALabelCondition;

/**
 * Represent an edge of a {@link GraphChunk}.
 * An edge must be a test on a label type.
 * 
 * @author zuri
 * @param <LBL> Label type to test.
 * @param <ELMNT>
 */
public interface IGCEdge<LBL> // extends Predicate<LBL>
{
	/**
	 * If the edge is a classic one, get its label as a {@link String}.
	 */
	Optional<String> getLabelAsString();

	IFSALabelCondition<LBL> getLabelCondition();
}