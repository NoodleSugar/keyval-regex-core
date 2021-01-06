package insomnia.unifier;

import insomnia.data.IPath;

public abstract class AbstractPathUnifierFactory<V, E, U extends IPathUnifier<V, E>> implements IPathUnifierFactory<V, E, U>
{
	@Override
	public U create(IPath<V, E> pb, IPath<V, E> sb, IPath<V, E> ph, IPath<V, E> sh)
	{
		return create(pb, sb, ph, sh, null);
	}
}
