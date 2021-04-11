package insomnia.implem.data;

import insomnia.data.AbstractVariable;
import insomnia.data.IVariable;

public class Variables
{
	private Variables()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class Variable<VAL> extends AbstractVariable<VAL>
	{
		private VAL value;

		Variable(VAL value)
		{
			this.value = value;
		}

		@Override
		public VAL getValue()
		{
			return value;
		}

		@Override
		public Object getID()
		{
			return this;
		}

		@Override
		public int hashCode()
		{
			return System.identityHashCode(this);
		}

		@Override
		public String toString()
		{
			var sbuilder = new StringBuilder("?").append(hashCode());

			if (null != value)
				sbuilder.append("=").append(value);

			return sbuilder.toString();
		}
	}

	// =========================================================================

	public interface INamedVariable<VAL> extends IVariable<VAL>
	{
		String getName();
	}

	private static class NamedVariable<VAL> extends Variable<VAL> implements INamedVariable<VAL>
	{
		private String name;

		NamedVariable(String name, VAL value)
		{
			super(value);
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public String toString()
		{
			return new StringBuilder().append(super.toString()).append("(").append(name).append(")").toString();
		}
	}
	// =========================================================================

	public static <VAL> Variable<VAL> create(VAL value)
	{
		return new Variable<>(value);
	}

	public static <VAL> NamedVariable<VAL> createNamed(String name, VAL value)
	{
		return new NamedVariable<>(name, value);
	}
}
