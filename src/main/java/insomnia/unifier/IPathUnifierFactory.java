package insomnia.unifier;

import insomnia.data.IPath;

public interface IPathUnifierFactory<V, E, U extends IPathUnifier<V, E>>
{
	U create(IPath<V, E> pb, IPath<V, E> sb, IPath<V, E> ph, IPath<V, E> sh, IPath<V, E> ref);

	U create(IPath<V, E> pb, IPath<V, E> sb, IPath<V, E> ph, IPath<V, E> sh);
}
