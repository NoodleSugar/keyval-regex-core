package insomnia.rule;

import java.util.Collection;

import insomnia.data.ITree;

/**
 * A general rule composed of a tree body and head (body -> head).
 * The body and the share conceptually the root node, so the rooted nature of the trees must be the same.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public interface IRule<VAL, LBL>
{
	ITree<VAL, LBL> getBody();

	ITree<VAL, LBL> getHead();

	/**
	 * The head have some existential nodes (ie. not frontier node).
	 */
	boolean isExistential();

	Collection<LBL> getVocabulary();
}
