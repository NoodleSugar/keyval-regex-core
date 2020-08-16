package insomnia.rule;

import insomnia.rule.tree.Path;

public class PathRule implements IRule<Path>
{
	private Path context;
	private Path foot;
	private Path body;
	private Path head;
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
	public Path getContext()
	{
		return context;
	}

	@Override
	public Path getFoot()
	{
		return foot;
	}

	@Override
	public Path getBody()
	{
		return body;
	}

	@Override
	public Path getHead()
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
