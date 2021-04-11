package insomnia.data;

public abstract class AbstractValuedNode<VAL, LBL> extends AbstractNode<VAL, LBL>
{
	VAL value;

	public AbstractValuedNode(VAL value)
	{
		super();
		setValue(value);
	}

	@Override
	public void setValue(VAL value)
	{
		this.value = value;
	}

	@Override
	public VAL getValue()
	{
		return value;
	}
}
