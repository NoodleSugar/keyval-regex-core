package insomnia.implem.data;

import insomnia.data.INode;

public final class Nodes
{
	private Nodes()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static abstract class Node<VAL, LBL> implements INode<VAL, LBL>
	{
		private VAL value;

		Node(VAL value)
		{
			this.value = value;
		}

		@Override
		public VAL getValue()
		{
			return value;
		}

		@Override
		public String toString()
		{
			return INode.toString(this);
		}
	}

	// =========================================================================

	/**
	 * Create a new node.
	 * 
	 * @param isRooted   the node is rooted
	 * @param isTerminal the node is terminal
	 * @param value      the value of the node
	 */
	public static <VAL, LBL> INode<VAL, LBL> create(boolean isRooted, boolean isTerminal, VAL value)
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

	/**
	 * Create a new node copying 'src'.
	 * 
	 * @param src the node to copy
	 */
	public static <VAL, LBL> INode<VAL, LBL> create(INode<VAL, ?> src)
	{
		return create(src.isRooted(), src.isTerminal(), src.getValue());
	}

	/**
	 * Create a new node copying 'src' but set the value to 'value'.
	 * 
	 * @param src   the node to copy
	 * @param value the value for the new node
	 */
	public static <VAL, LBL> INode<VAL, LBL> create(INode<?, ?> src, VAL value)
	{
		return create(src.isRooted(), src.isTerminal(), value);
	}
}
