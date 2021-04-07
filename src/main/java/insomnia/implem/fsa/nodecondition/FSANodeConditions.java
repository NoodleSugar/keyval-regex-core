package insomnia.implem.fsa.nodecondition;

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

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createProjection(IGFPA<VAL, LBL> automaton, IFSAState<VAL, LBL> state)
	{
		return new IFSANodeCondition<VAL, LBL>()
		{
			private final boolean rootedCheck, terminalCheck;
			{
				rootedCheck   = !automaton.isRooted(state);
				terminalCheck = !automaton.isTerminal(state);
			}

			@Override
			public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
			{
				return (terminalCheck || node.isTerminal()) && (rootedCheck || node.isRooted());
			}

			@Override
			public String toString()
			{
				return "Π";
			}
		};
	}

	// =========================================================================

	private static class FSANodeAny<VAL, LBL> implements IFSANodeCondition<VAL, LBL>
	{
		@Override
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

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createRooted(boolean rootedv)
	{
		return new IFSANodeCondition<VAL, LBL>()
		{
			boolean rooted;
			String  s;
			{
				rooted = rootedv;
				s      = rootedv ? "^" : "!^";
			}

			@Override
			public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
			{
				return node.isRooted() == rooted;
			}

			@Override
			public String toString()
			{
				return s;
			}
		};
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createTerminal(boolean terminalv)
	{
		return new IFSANodeCondition<VAL, LBL>()
		{
			boolean terminal;
			String  s;
			{
				terminal = terminalv;
				s        = terminalv ? "$" : "!$";
			}

			@Override
			public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
			{
				return node.isTerminal() == terminal;
			}

			@Override
			public String toString()
			{
				return s;
			}
		};
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> create(boolean rootedv, boolean terminalv)
	{
		return new IFSANodeCondition<VAL, LBL>()
		{
			boolean rooted, terminal;
			String  s;
			{
				rooted    = rootedv;
				terminal  = terminalv;
				s         = rootedv ? "^" : "!^";
				s        += terminalv ? "$" : "!$";
			}

			@Override
			public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
			{
				return node.isRooted() == rooted && node.isTerminal() == terminal;
			}

			@Override
			public String toString()
			{
				return s;
			}
		};
	}
}
