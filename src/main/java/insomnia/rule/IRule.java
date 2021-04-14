package insomnia.rule;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.IterableUtils;

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

	boolean frontierIsTerminal();

	boolean isExistential(INode<VAL, LBL> node);

	boolean isFrontier(INode<VAL, LBL> node);

	Collection<LBL> getVocabulary();

	/**
	 * Get the frontier of the rule from head to body.
	 * 
	 * @return the mappings associating a head {@link INode} to a body {@link INode}
	 */
	Map<INode<VAL, LBL>, INode<VAL, LBL>> getFontier();

	Collection<INode<VAL, LBL>> getExistentialNodes();

	// =========================================================================

	static <VAL, LBL> boolean frontierIsTerminal(IRule<VAL, LBL> rule)
	{
		var bodyRoot = rule.getBody().getRoot();
		return IterableUtils.matchesAll(rule.getFontier().values(), n -> n == bodyRoot || n.isTerminal());
	}
}