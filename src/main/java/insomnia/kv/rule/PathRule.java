package insomnia.kv.rule;

import java.util.regex.Pattern;

import insomnia.data.tree.Path;

public class PathRule implements IRule<Path>
{
	public final static String separator = Pattern.quote(".");

	private Path context;
	private Path foot;
	private Path body;
	private Path head;

	private boolean isExistential;

	public static PathRule create(String body, String head)
	{
		return create(body, head, false, false);
	}

	public static PathRule create(String body, String head, boolean isRooted, boolean isTerminal)
	{
		return new PathRule( //
			new Path(isRooted, isTerminal, body.split(separator)), //
			new Path(isRooted, isTerminal, head.split(separator)), //
			false);
	}

	public static PathRule createExistential(String body, String head, boolean isRooted, boolean isBodyTerminal, boolean isHeadTerminal)
	{
		return new PathRule(//
			new Path(isRooted, isBodyTerminal, body.split(separator)), //
			new Path(isRooted, isHeadTerminal, head.split(separator)), //
			true);
	}

	public PathRule(Path body, Path head, boolean isExistential) throws IllegalArgumentException
	{
		if (body.isRooted() != head.isRooted())
			throw new IllegalArgumentException("body and head must have the same isRooted value");

		if (!isExistential && body.isTerminal() != head.isTerminal())
			throw new IllegalArgumentException("body and head must have the same isTerminal value for non existential rule");

//		context = null;
//		foot = null;

		this.body          = body;
		this.head          = head;
		this.isExistential = isExistential;
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
		return body.isRooted();
	}

	@Override
	public boolean isTerminal()
	{
		return body.isTerminal();
	}

	@Override
	public boolean isExistential()
	{
		return isExistential;
	}
}
