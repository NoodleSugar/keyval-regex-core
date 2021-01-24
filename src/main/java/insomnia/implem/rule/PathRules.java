package insomnia.implem.rule;

import java.util.Collection;
import java.util.HashSet;

import insomnia.data.IPath;
import insomnia.rule.IPathRule;

public final class PathRules<VAL, LBL>
{
	private PathRules()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static class PathRule<VAL, LBL> implements IPathRule<VAL, LBL>
	{
		private IPath<VAL, LBL> body, head;

		private boolean isExistential;

		private PathRule(IPath<VAL, LBL> body, IPath<VAL, LBL> head, boolean isExistential) throws IllegalArgumentException
		{
			if (body.isRooted() != head.isRooted())
				throw new IllegalArgumentException("body and head must have the same isRooted value");

			if (!isExistential && body.isTerminal() != head.isTerminal())
				throw new IllegalArgumentException("body and head must have the same isTerminal value for non existential rule");

			this.body          = body;
			this.head          = head;
			this.isExistential = isExistential;
		}

		@Override
		public IPath<VAL, LBL> getBody()
		{
			return body;
		}

		@Override
		public IPath<VAL, LBL> getHead()
		{
			return head;
		}

		@Override
		public boolean isExistential()
		{
			return isExistential;
		}

		@Override
		public Collection<LBL> getVocabulary()
		{
			Collection<LBL> ret = new HashSet<>();
			ret.addAll(body.getVocabulary());
			ret.addAll(head.getVocabulary());
			return ret;
		}

		@Override
		public String toString()
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(body).append(" -> ");

			if (isExistential)
				buffer.append("∃");

			buffer.append(head);
			return buffer.toString();
		}
	}

	// =========================================================================

	public static <VAL, LBL> IPathRule<VAL, LBL> create(IPath<VAL, LBL> body, IPath<VAL, LBL> head)
	{
		return create(body, head, false);
	}

	public static <VAL, LBL> IPathRule<VAL, LBL> create(IPath<VAL, LBL> body, IPath<VAL, LBL> head, boolean isExistential)
	{
		return new PathRule<>(body, head, isExistential);
	}
}