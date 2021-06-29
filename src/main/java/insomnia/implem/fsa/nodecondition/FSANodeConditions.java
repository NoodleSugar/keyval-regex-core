package insomnia.implem.fsa.nodecondition;

import java.security.InvalidParameterException;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.fsa.IFSANodeCondition;

public final class FSANodeConditions
{
	private FSANodeConditions()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class NodeCondition<VAL, LBL> implements IFSANodeCondition<VAL, LBL>
	{
		boolean rootedVal, terminalVal;
		boolean checkRootedVal, checkTerminalVal;

		public NodeCondition(boolean checkr, boolean r, boolean checkt, boolean t)
		{
			checkRootedVal   = checkr;
			checkTerminalVal = checkt;
			// A value must be false if its check is inactive
			rootedVal   = checkr ? r : false;
			terminalVal = checkt ? t : false;
		}

		@Override
		public boolean test(ITree<VAL, LBL> tree, INode<VAL, LBL> node)
		{
			return (!checkRootedVal || node.isRooted() == rootedVal) && (!checkTerminalVal || node.isTerminal() == terminalVal);
		}

		@Override
		public int hashCode()
		{
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public boolean equals(Object obj)
		{
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			if (checkRootedVal)
				sb.append(rootedVal ? "^" : "!^");

			if (checkTerminalVal)
				sb.append(terminalVal ? "$" : "!$");

			return sb.toString();
		}
	};

	// =========================================================================

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createProjection()
	{
		return createProjection(false, false);
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createProjection(boolean checkRooted, boolean checkTerminal)
	{
		return new NodeCondition<>(checkRooted, true, checkTerminal, true);
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

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createRooted(boolean rooted)
	{
		return createEq(rooted, false);
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createTerminal(boolean terminal)
	{
		return createEq(false, terminal);
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> createEq(boolean rooted, boolean terminal)
	{
		return new NodeCondition<>(true, rooted, true, terminal);
	}

	// =========================================================================

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> union(IFSANodeCondition<VAL, LBL> a, IFSANodeCondition<VAL, LBL> b)
	{
		if (isAny(a) || Objects.equals(a, b))
			return a;
		if (isAny(b))
			return b;

		if (!(a instanceof NodeCondition<?, ?>) || !(b instanceof NodeCondition<?, ?>))
			throw new InvalidParameterException();

		var aa = (NodeCondition<VAL, LBL>) a;
		var bb = (NodeCondition<VAL, LBL>) b;

		boolean checkRootedVal, rootedVal = false;
		boolean checkTerminalVal, terminalVal = false;

		if (aa.rootedVal != bb.rootedVal)
			checkRootedVal = false;
		else
		{
			checkRootedVal = true;
			rootedVal      = aa.rootedVal;
		}

		if (aa.terminalVal != bb.terminalVal)
			checkTerminalVal = false;
		else
		{
			checkTerminalVal = true;
			terminalVal      = aa.terminalVal;
		}
		return new NodeCondition<>(checkRootedVal, rootedVal, checkTerminalVal, terminalVal);
	}

	public static <VAL, LBL> IFSANodeCondition<VAL, LBL> intersection(IFSANodeCondition<VAL, LBL> a, IFSANodeCondition<VAL, LBL> b)
	{
		if (isAny(a) || Objects.equals(a, b))
			return b;
		if (isAny(b))
			return a;

		if (!(a instanceof NodeCondition<?, ?>) || !(b instanceof NodeCondition<?, ?>))
			throw new InvalidParameterException();

		var aa = (NodeCondition<VAL, LBL>) a;
		var bb = (NodeCondition<VAL, LBL>) b;

		boolean checkRootedVal   = aa.checkRootedVal || bb.checkRootedVal;
		boolean checkTerminalVal = aa.checkTerminalVal || bb.checkTerminalVal;
		boolean terminalVal      = false;
		boolean rootedVal        = false;

		if (checkRootedVal)
		{
			if (aa.checkRootedVal && bb.checkRootedVal && aa.rootedVal != bb.rootedVal)
				throw new InvalidParameterException();

			rootedVal = aa.checkRootedVal ? aa.rootedVal : bb.rootedVal;
		}

		if (checkTerminalVal)
		{
			if (aa.checkTerminalVal && bb.checkTerminalVal && aa.terminalVal != bb.terminalVal)
				throw new InvalidParameterException();

			terminalVal = aa.checkTerminalVal ? aa.terminalVal : bb.terminalVal;
		}
		return new NodeCondition<>(checkRootedVal, rootedVal, checkTerminalVal, terminalVal);
	}
}
