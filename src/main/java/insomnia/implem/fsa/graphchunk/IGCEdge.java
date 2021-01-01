package insomnia.implem.fsa.graphchunk;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represent an edge of a {@link GraphChunk}.
 * An edge must be a test on a label type.
 * 
 * @author zuri
 * @param <LBL> Label type to test.
 * @param <ELMNT>
 */
public interface IGCEdge<LBL> extends Predicate<LBL>
{
	/**
	 * If the edge is a classic one, get its label as a {@link String}.
	 */
	Optional<String> getLabelAsString();

	/**
	 * Get the internal object serving for equality test (ie. label).
	 */
	Object getObj();

	@Override
	boolean test(LBL t);
}