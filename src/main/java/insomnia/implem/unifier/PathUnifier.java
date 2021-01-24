package insomnia.implem.unifier;

import java.util.Arrays;

import insomnia.data.IPath;
import insomnia.implem.data.Paths;
import insomnia.unifier.AbstractPathUnifier;
import insomnia.unifier.IPathUnifier;

public final class PathUnifier<VAL, LBL> extends AbstractPathUnifier<VAL, LBL>
{
	public PathUnifier(IPathUnifier<VAL, LBL> unifier)
	{
		this(unifier.getPrefixBody(), //
			unifier.getSuffixBody(), //
			unifier.getPrefixHead(), //
			unifier.getSuffixHead(), //
			unifier.getReference() //
		);
	}

	public PathUnifier(IPath<VAL, LBL> pb, IPath<VAL, LBL> sb, IPath<VAL, LBL> ph, IPath<VAL, LBL> sh, IPath<VAL, LBL> ref)
	{
		super(pb, sb, ph, sh, ref);
	}

	@Override
	protected IPath<VAL, LBL> emptyPath()
	{
		return Paths.create();
	}

	@Override
	public IPath<VAL, LBL> getBody()
	{
		if (getPrefixBody().isEmpty() && getSuffixBody().isEmpty())
			return Paths.create();

		return Paths.concat(Arrays.asList(getPrefixBody(), getSuffixBody()));
	}

	@Override
	public IPath<VAL, LBL> getHead()
	{
		if (getPrefixHead().isEmpty() && getSuffixHead().isEmpty())
			return Paths.create();

		return Paths.concat(Arrays.asList(getPrefixHead(), getSuffixHead()));
	}
}
