package insomnia.rule.grd;

import java.util.Collection;

import org.jgrapht.Graph;

import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;

/**
 * Graph of Rule Dependency
 */
public interface IGRD<V, E> extends Graph<IRule<V, E>, IDependency<V, E>>
{
	/**
	 * Get the dependencies of rule with the rules presents in the GRD.
	 * The rule may not be in the GRD (useful for retrieve dependencies of a query or a data).
	 * 
	 * @param rule
	 * @return
	 */
	Collection<IDependency<V, E>> getDependencies(IRule<V, E> rule);
}
