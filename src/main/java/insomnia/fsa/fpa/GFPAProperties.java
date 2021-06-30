package insomnia.fsa.fpa;

import java.util.EnumSet;

import org.apache.commons.collections4.CollectionUtils;

public final class GFPAProperties
{
	private GFPAProperties()
	{
		throw new AssertionError();
	}

	static public void union(EnumSet<GFPAProperty> dest, EnumSet<GFPAProperty> src)
	{
		dest.retainAll(CollectionUtils.intersection(dest, src));
	}
}
