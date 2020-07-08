package insomnia.rule.tree;

public class TreeException extends Exception
{
	private static final long serialVersionUID = -4333797420730413873L;

	public TreeException(String message)
	{
		super(message);
	}
	
	public TreeException(Throwable cause)
	{
		super(cause);
	}
	
	public TreeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
