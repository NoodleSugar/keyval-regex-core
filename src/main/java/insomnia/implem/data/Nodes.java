package insomnia.implem.data;

import insomnia.data.AbstractValuedNode;
import insomnia.data.INode;

public final class Nodes
{
	private Nodes()
	{
		throw new AssertionError();
	}

	// ==========================================================================

	private static abstract class Node<VAL, LBL> extends AbstractValuedNode<VAL, LBL>
	{
		Node(VAL value)
		{
			super(value);
		}
	}

	// ==========================================================================

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
	 * Create a new empty node non rooted/terminal
	 */
	public static <VAL, LBL> INode<VAL, LBL> create()
	{
		return create(false, false, null);
	}

	/**
	 * Create a new node non rooted/terminal, with a value
	 * 
	 * @param value the value for the new node
	 */
	public static <VAL, LBL> INode<VAL, LBL> create(VAL value)
	{
		return create(false, false, value);
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
