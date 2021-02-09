package insomnia.data;

import java.util.List;
import java.util.Optional;

/**
 * Immutable Path where the leaf may have a value.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public interface IPath<VAL, LBL> extends ITree<VAL, LBL>
{
	/**
	 * Such as {@link String#substring(int, int)} get a sub-path from the current one.
	 * The isRooted/isTerminal nature count as a part of the path (like in {@link IPath#size()}.
	 * 
	 * @param begin Inclusive offset.
	 * @param end   Exclusive end index.
	 * @return The sub path.
	 */
	IPath<VAL, LBL> subPath(int from, int to);

	/**
	 * @return the labels of the path conserving the path order.
	 */
	List<LBL> getLabels();

	/**
	 * @return the child edge of 'node' if exists.
	 */
	Optional<IEdge<VAL, LBL>> getChild(INode<VAL, LBL> node);

	/**
	 * @return the value of the leaf node if set.
	 */
	Optional<VAL> getValue();

	/**
	 * @return the number of labels.
	 */
	int nbLabels();

	/**
	 * The size of a path is the number of labels plus the isRooted or/and isTerminal natures.
	 * 
	 * @return the size of the path.
	 */
	int size();

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