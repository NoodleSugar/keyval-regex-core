package insomnia.implem.FSA;

public class GCState
{
	private Integer id;

	public GCState(Integer id)
	{
		this.id = id;
	}

	public Integer getId()
	{
		return id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof GCState))
			return false;

		return id == ((GCState) obj).id;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return "<:" + id + ":>";
	}
}
