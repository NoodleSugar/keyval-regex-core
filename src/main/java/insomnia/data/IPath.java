package insomnia.data;

import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import insomnia.data.PathOp.RealLimits;

/**
 * An immutable Path.
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
	 * @return the labels of the path conserving the path order
	 */
	List<LBL> getLabels();

	/**
	 * @return nodes in order
	 */
	List<INode<VAL, LBL>> getNodes();

	/**
	 * @return values in order
	 */
	List<VAL> getValues();

	INode<VAL, LBL> getLeaf();

	/**
	 * @return the child edge of 'node' if exists, or {@code null}
	 */
	IEdge<VAL, LBL> getChild(INode<VAL, LBL> node);

	int nbLabels();

	int nbNodes();

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

	// =========================================================================

	/**
	 * Get the informations needed for an {@link IPath#subPath(int, int)} implementation.
	 * 
	 * @param path the path to consider
	 * @param from the inclusive index
	 * @param to   the exclusive index
	 * @return the limits, values and nodes to get to the new path; or null if an empty path must be create
	 */
	public static <VAL, LBL> Triple<RealLimits, List<LBL>, List<INode<VAL, LBL>>> subPathInfos(IPath<VAL, LBL> path, int from, int to)
	{
		assert (from >= 0 && to >= from);

		if (from == to)
			return null;

		RealLimits limits = PathOp.realLimits(path, from, to);
		return Triple.of(limits, path.getLabels().subList(limits.getFrom(), limits.getTo()), path.getNodes().subList(limits.getFrom(), limits.getTo() + 1));
	}

}