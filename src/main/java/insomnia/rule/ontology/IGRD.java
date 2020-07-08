package insomnia.rule.ontology;

import java.util.List;

import insomnia.rule.IRule;

/**
 * Graph of Rule Dependency
 */
public interface IGRD<R extends IRule<?>>
{
	boolean isCyclic();
	
	List<R> closure(R rule, boolean materialization);

	List<R> getChildren(R rule);

	List<R> getParents(R rule);
}
