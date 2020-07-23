package insomnia.rule;

import insomnia.rule.tree.IPath;
import insomnia.rule.tree.Path;

public class PathRule implements IRule<IPath>
{
	private IPath context;
	private IPath foot;
	private IPath body;
	private IPath head;
	private boolean rooted;
	private boolean valued;
	private boolean existential;

	public static PathRule create(String b, String h, boolean isRooted, boolean isValued)
	{
		return new PathRule(b, h, isRooted, isValued, isValued, false);
	}

	public static PathRule createExistential(String b, String h, boolean isRooted, boolean isValued)
	{
		return new PathRule(b, h, isRooted, isValued, false, true);
	}

	public PathRule(String b, String h)
	{
		this(b, h, false, false, false, false);
	}

	private PathRule(String b, String h, boolean isRooted, boolean isBodyValued, boolean isHeadValued,
			boolean isExistential)
	{
		context = null;
		foot = null;
		body = new Path(b, isRooted, isBodyValued);
		head = new Path(h, isRooted, isHeadValued);
		rooted = isRooted;
		valued = isBodyValued;
		existential = isExistential;
	}

	@Override
	public IPath getContext()
	{
		return context;
	}

	@Override
	public IPath getFoot()
	{
		return foot;
	}

	@Override
	public IPath getBody()
	{
		return body;
	}

	@Override
	public IPath getHead()
	{
		return head;
	}

	@Override
	public boolean isRooted()
	{
		return rooted;
	}

	@Override
	public boolean isValued()
	{
		return valued;
	}

	@Override
	public boolean isExistential()
	{
		return existential;
	}
}
