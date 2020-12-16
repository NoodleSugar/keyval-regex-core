package insomnia.rule.grd;

import org.jgrapht.Graph;

import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;

/**
 * Graph of Rule Dependency
 */
public interface IGRD<V, E> extends Graph<IRule<V, E>, IDependency<V, E>>
{
}
