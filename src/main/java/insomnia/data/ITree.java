package insomnia.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ITree<VAL, LBL>
{
	INode<VAL, LBL> getRoot();

	/**
	 * The root must be a true root.
	 */
	boolean isRooted();

	List<? extends IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node);

	Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node);

	Collection<LBL> getVocabulary();
}