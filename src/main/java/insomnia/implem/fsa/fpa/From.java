package insomnia.implem.fsa.fpa;

class From
{
	private boolean isRooted;

	private int from;

	public From(boolean isRooted, int offset)
	{
		this.isRooted = isRooted;
		this.from     = offset;
	}

	public boolean isRooted()
	{
		return isRooted;
	}

	public int getFrom()
	{
		return from;
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append(isRooted).append(", ").append(from).toString();
	}
}
