package insomnia.rule.ontology;

import java.util.List;

import insomnia.rule.IRule;

/**
 * Graph of Rule Dependency
 */
public interface IGRD<R extends IRule<?>>
{
	public enum DependencyMode
	{
		WEAK, STRONG
	}

	List<R> closure(R rule, DependencyMode mode);

	default List<R> closureAll(R rule)
	{
		List<R> cl = closure(rule, DependencyMode.WEAK);
		cl.addAll(closure(rule, DependencyMode.STRONG));
		return cl;
	}

	List<R> reverseClosure(R rule, DependencyMode mode);

	default List<R> reverseClosureAll(R rule)
	{
		List<R> cl = reverseClosure(rule, DependencyMode.WEAK);
		cl.addAll(reverseClosure(rule, DependencyMode.STRONG));
		return cl;
	}

	List<R> getChildren(R rule, DependencyMode mode);

	default List<R> getChildrenAll(R rule)
	{
		List<R> children = getChildren(rule, DependencyMode.WEAK);
		children.addAll(getChildren(rule, DependencyMode.STRONG));
		return children;
	}

	List<R> getParents(R rule, DependencyMode mode);

	default List<R> getParentsAll(R rule)
	{
		List<R> parents = getParents(rule, DependencyMode.WEAK);
		parents.addAll(getParents(rule, DependencyMode.STRONG));
		return parents;
	}
}
