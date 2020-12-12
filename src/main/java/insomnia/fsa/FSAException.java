package insomnia.fsa;

public class FSAException extends Exception
{
	private static final long serialVersionUID = 759935166323836284L;
	
	public FSAException(String message)
	{
		super(message);
	}
	
	public FSAException(Throwable cause)
	{
		super(cause);
	}
	
	public FSAException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
