package insomnia.automaton;

public class AutomatonException extends Exception
{
	private static final long serialVersionUID = 759935166323836284L;
	
	public AutomatonException(String message)
	{
		super(message);
	}
	
	public AutomatonException(Throwable cause)
	{
		super(cause);
	}
	
	public AutomatonException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
