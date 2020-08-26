package insomnia.rule.tree.value;

public interface IValue
{
	public enum Type
	{
		NUMBER, //
		STRING;
	}

	Type getType();

	Object getValue();
}
