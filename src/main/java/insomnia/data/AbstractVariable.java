package insomnia.data;

public abstract class AbstractVariable<VAL> implements IVariable<VAL>
{
	@Override
	public abstract int hashCode();

	@Override
	public final boolean equals(Object obj)
	{
		return this == obj || getID() == ((IVariable<?>) obj).getID();
	}
}
