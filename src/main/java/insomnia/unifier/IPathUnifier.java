package insomnia.unifier;

import insomnia.data.IPath;

public interface IPathUnifier<V, E> extends IUnifier<V, E>
{
	IPath<V, E> getPrefixHead();

	IPath<V, E> getPrefixBody();

	IPath<V, E> getSuffixHead();

	IPath<V, E> getSuffixBody();

	@Override
	IPath<V, E> getBody();

	@Override
	IPath<V, E> getHead();

	@Override
	IPath<V, E> getReference();
}
