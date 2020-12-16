package insomnia.implem.kv.unifier;

import insomnia.data.IPath;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPath;
import insomnia.implem.kv.data.KVValue;
import insomnia.unifier.AbstractPathUnifier;
import insomnia.unifier.IPathUnifier;

public class KVPathUnifier extends AbstractPathUnifier<KVValue, KVLabel>
{
	public KVPathUnifier(IPathUnifier<KVValue, KVLabel> unifier)
	{
		this(unifier.getPrefixBody(), //
			unifier.getSuffixBody(), //
			unifier.getPrefixHead(), //
			unifier.getSuffixHead(), //
			unifier.getReference() //
		);
	}

	public KVPathUnifier(IPath<KVValue, KVLabel> pb, IPath<KVValue, KVLabel> sb, IPath<KVValue, KVLabel> ph, IPath<KVValue, KVLabel> sh, IPath<KVValue, KVLabel> ref)
	{
		super(pb, sb, ph, sh, ref);
	}

	@Override
	protected IPath<KVValue, KVLabel> emptyPath()
	{
		return new KVPath();
	}

	@Override
	public IPath<KVValue, KVLabel> getBody()
	{
		return getPrefixBody();
	}

	@Override
	public IPath<KVValue, KVLabel> getHead()
	{
		return getSuffixHead();
	}
}
