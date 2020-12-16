package insomnia.implem.kv.rule.dependency;

import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.unifier.IUnifier;

public interface IKVDependency<V, E> extends IUnifier<V, E>, IDependency<V, E>
{
	IRule<V, E> getSource();

	IRule<V, E> getTarget();
}
