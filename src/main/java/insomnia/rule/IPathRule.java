package insomnia.rule;

import insomnia.data.IPath;

public interface IPathRule<V, E> extends IRule<V, E>
{
	@Override
	IPath<V, E> getBody();

	@Override
	IPath<V, E> getHead();
}
