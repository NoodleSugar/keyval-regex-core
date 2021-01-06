package insomnia.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Representation of an immutable Tree.
 * 
 * @author zuri
 * @param <VAL> Type of the values from nodes.
 * @param <LBL> Type of the labels from edges.
 */
public interface ITree<VAL, LBL>
{
	/**
	 * @return The root of the tree or null if absent.
	 */
	INode<VAL, LBL> getRoot();

	/**
	 * The root must be a true root.
	 */
	boolean isRooted();

	List<? extends IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node);

	Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node);

	Collection<LBL> getVocabulary();
}