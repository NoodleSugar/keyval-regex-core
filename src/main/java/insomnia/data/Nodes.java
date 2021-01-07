package insomnia.data;

import java.util.Optional;

public final class Nodes
{
	private Nodes()
	{

	}

	// =========================================================================

	private static abstract class Node<VAL, LBL> implements INode<VAL, LBL>
	{
		private Optional<VAL> value;

		Node(Optional<VAL> value)
		{
			this.value = value;
		}

		@Override
		public Optional<VAL> getValue()
		{
			return value;
		}
	}

	// =========================================================================

	public static <VAL, LBL> INode<VAL, LBL> create(boolean isRooted, boolean isTerminal, Optional<VAL> value)
	{
		return new Node<VAL, LBL>(value)
		{
			@Override
			public boolean isTerminal()
			{
				return isTerminal;
			}

			@Override
			public boolean isRooted()
			{
				return isRooted;
			}
		};
	}
}
