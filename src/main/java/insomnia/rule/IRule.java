package insomnia.rule;

import java.util.Collection;
import java.util.Map;

import insomnia.data.INode;
import insomnia.data.ITree;

/**
 * A general rule composed of a tree body and head (body -> head).
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

	boolean isExistential(INode<VAL, LBL> node);

	boolean isFrontier(INode<VAL, LBL> node);

	Collection<LBL> getVocabulary();

	/**
	 * Get the frontier of the rule.
	 * The mapping relation is bidirectional.
	 * 
	 * @return the mappings between the frontier nodes of body and head
	 */
	Map<INode<VAL, LBL>, INode<VAL, LBL>> getFontier();

	Collection<INode<VAL, LBL>> getExistentialNodes();
}
