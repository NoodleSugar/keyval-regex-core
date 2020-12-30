package insomnia.data;

import java.util.Collection;
import java.util.List;

public interface ITree<VAL, LBL>
{
	INode<VAL, LBL> getRoot();

	/**
	 * The root must be a true root.
	 */
	boolean isRooted();

	List<? extends IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node);

	IEdge<VAL, LBL> getParent(INode<VAL, LBL> node);

	Collection<LBL> getVocabulary();
}