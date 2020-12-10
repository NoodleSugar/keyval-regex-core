package insomnia.unifier;

import org.apache.commons.lang3.ObjectUtils;

import insomnia.data.IPath;

public abstract class AbstractPathUnifier<V, E> implements IPathUnifier<V, E>
{
	public IPath<V, E> prefixBody;
	public IPath<V, E> suffixBody;
	public IPath<V, E> prefixHead;
	public IPath<V, E> suffixHead;
	public IPath<V, E> reference;

	abstract protected IPath<V, E> emptyPath();
	
	protected AbstractPathUnifier(IPath<V, E> pb, IPath<V, E> sb, IPath<V, E> ph, IPath<V, E> sh, IPath<V, E> ref)
	{
		prefixBody = ObjectUtils.defaultIfNull(pb, emptyPath());
		suffixBody = ObjectUtils.defaultIfNull(sb, emptyPath());
		prefixHead = ObjectUtils.defaultIfNull(ph, emptyPath());
		suffixHead = ObjectUtils.defaultIfNull(sh, emptyPath());
		reference  = ObjectUtils.defaultIfNull(ref, emptyPath());
	}

	public IPath<V, E> getPrefixHead()
	{
		return prefixHead;
	}

	public IPath<V, E> getPrefixBody()
	{
		return prefixBody;
	}

	public IPath<V, E> getSuffixHead()
	{
		return suffixHead;
	}

	public IPath<V, E> getSuffixBody()
	{
		return suffixBody;
	}

	public IPath<V, E> getReference()
	{
		return reference;
	}

	public boolean emptyBody()
	{
		return prefixBody.isEmpty() && suffixBody.isEmpty();
	}

	public boolean emptyHead()
	{
		return prefixHead.isEmpty() && suffixHead.isEmpty();
	}

	public boolean isWeak()
	{
		return !emptyBody();
	}

	public boolean isStrong()
	{
		return emptyBody();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;

		if (!(obj instanceof AbstractPathUnifier))
			return false;

		@SuppressWarnings("unchecked")
		AbstractPathUnifier<V, E> u = (AbstractPathUnifier<V, E>) obj;
		return this.prefixBody.equals(u.prefixBody) //
			&& this.suffixBody.equals(u.suffixBody) //
			&& this.prefixHead.equals(u.prefixHead) //
			&& this.suffixHead.equals(u.suffixHead) //
			&& this.reference.equals(u.reference);
	}

	@Override
	public int hashCode()
	{
		return this.prefixBody.hashCode() //
			+ this.suffixBody.hashCode() //
			+ this.prefixHead.hashCode() //
			+ this.suffixHead.hashCode() //
			+ this.reference.hashCode();
	}

	@Override
	public String toString()
	{
		return "B:" + prefixBody + "_" + suffixBody //
			+ " H:" + prefixHead + "_" + suffixHead //
			+ " _:" + reference;
	}
}
