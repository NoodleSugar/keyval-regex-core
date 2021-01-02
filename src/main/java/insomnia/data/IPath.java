package insomnia.data;

import java.util.List;
import java.util.Optional;

/**
 * Path where the leaf may have a value.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public interface IPath<VAL, LBL> extends ITree<VAL, LBL>
{
	/**
	 * Such as {@link String#substring(int, int)} get a subpath from the current one.
	 * 
	 * @param begin Inclusive offset.
	 * @param end   Exclusive end index.
	 * @return
	 */
	IPath<VAL, LBL> subPath(int begin, int end);

	IPath<VAL, LBL> setValue(VAL value);

	/**
	 * Get the labels of the path conserving the path order.
	 */
	List<LBL> getLabels();

	Optional<IEdge<VAL, LBL>> getChild(INode<VAL, LBL> node);

	Optional<VAL> getValue();

	int nbLabels();

	int size();

	boolean isEmpty();

	/**
	 * The leaf must be a true leaf.
	 * The value must not be necessary set.
	 */
	boolean isTerminal();

	/**
	 * Rooted and Terminal.
	 */
	boolean isComplete();

	/**
	 * Nor rooted nor terminal.
	 */
	boolean isFree();

	/**
	 * Rooted or Terminal.
	 */
	boolean isFixed();
}