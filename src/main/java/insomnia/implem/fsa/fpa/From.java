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

	// ==========================================================================

	public boolean isRooted()
	{
		return isRooted;
	}

	public int getFrom()
	{
		return from;
	}

	// ==========================================================================

	@Override
	public int hashCode()
	{
		return Boolean.valueOf(isRooted).hashCode() + from * 31;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof From))
			return false;

		From from = (From) obj;
		return isRooted == from.isRooted && this.from == from.from;
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("(").append(isRooted).append(", ").append(from).append(")").toString();
	}
}
