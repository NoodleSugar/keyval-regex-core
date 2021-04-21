package insomnia.implem.fsa.nodecondition;

import org.apache.commons.lang3.BooleanUtils;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSANodeCondition;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;

public final class FSANodeConditions
{
	private FSANodeConditions()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static abstract class AbstractNodeCondition<VAL, LBL> implements IFSANodeCondition<VAL, LBL>
	{
		boolean rooted, terminal;

		public AbstractNodeCondition(boolean rooted, boolean terminal)
		{
			this.rooted   = rooted;
			this.terminal = terminal;
		}

		@Override
		public int hashCode()
		{
			return BooleanUtils.toInteger(rooted) + BooleanUtils.toInteger(terminal) * 2;
		}

		@Override
		public boolean equals(Object obj)
		{
			@SuppressWarnings("unchecked")
			AbstractNodeCondition<VAL, LBL> o = (AbstractNodeCondition<VAL, LBL>) obj;
			return rooted == o.rooted && terminal == o.terminal;
		}
	};

	// =========================================================================

	private static class Projection<VAL, LBL> extends AbstractNodeCondition<VAL, LBL>
	{
		Projection(boolean rootedCheck, boolean terminalCheck)
		{
			super(rootedCheck, terminalCheck);
		}

		@Override
		public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
		{
			return (terminal || node.isTerminal()) && (rooted || node.isRooted());
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
				return true;
			if (!(obj instanceof Projection<?, ?>))
				return false;

			return super.equals(obj);
		}

		@Override
		public String toString()
		{
			return "Π";
		}
	};

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createProjection(boolean checkRooted, boolean checkTerminal)
	{
		return new Projection<>(!checkRooted, !checkTerminal);
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createProjection(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return new Projection<>(!automaton.isRooted(state), !automaton.isTerminal(state));
	}

	// =========================================================================

	private static class FSANodeAny<VAL, LBL> implements IFSANodeCondition<VAL, LBL>
	{
		public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
		{
			return true;
		}

		@Override
		public String toString()
		{
			return "*";
		}
	}

	private static final IFSANodeCondition<Object, Object> any = new FSANodeAny<>();

	@SuppressWarnings("unchecked")
	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createAny()
	{
		return (IFSANodeCondition<VAL, LBL>) any;
	}

	public static <VAL, LBL> boolean isAny(IFSANodeCondition<VAL, LBL> vcond)
	{
		return vcond == any;
	}
	// =========================================================================

	private static class NodeCondition<VAL, LBL> extends AbstractNodeCondition<VAL, LBL>
	{
		public NodeCondition(boolean rooted, boolean terminal)
		{
			super(rooted, terminal);
		}

		@Override
		public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
		{
			return node.isRooted() == rooted && node.isTerminal() == terminal;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
				return true;
			if (!(obj instanceof NodeCondition<?, ?>))
				return false;
			return super.equals(obj);
		}

		@Override
		public String toString()
		{
			String s = rooted ? "^" : "!^";
			s += terminal ? "$" : "!$";
			return s;
		}
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createRooted(boolean rooted)
	{
		return create(rooted, false);
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createTerminal(boolean terminal)
	{
		return create(false, terminal);
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> create(boolean rooted, boolean terminal)
	{
		return new NodeCondition<>(rooted, terminal);
	}
}
