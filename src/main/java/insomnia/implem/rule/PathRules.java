package insomnia.implem.rule;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.implem.data.Paths;
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

		private Map<INode<VAL, LBL>, INode<VAL, LBL>> frontier;

		private PathRule(IPath<VAL, LBL> body, IPath<VAL, LBL> head, boolean isExistential) throws IllegalArgumentException
		{
			if (body.isRooted() != head.isRooted())
				throw new IllegalArgumentException("body and head must have the same isRooted value");

			if (!isExistential && body.isTerminal() != head.isTerminal())
				throw new IllegalArgumentException("body and head must have the same isTerminal value for non existential rule");

			if (null != body.getLeaf().getValue())
				throw new IllegalArgumentException("body cannot have a value");

			this.body          = body;
			this.head          = head;
			this.isExistential = isExistential;

			frontier = new DualHashBidiMap<>();
			frontier.put(head.getRoot(), body.getRoot());

			if (!isExistential)
				frontier.put(head.getLeaf(), body.getLeaf());

			frontier = MapUtils.unmodifiableMap(frontier);
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
		public Map<INode<VAL, LBL>, INode<VAL, LBL>> getFontier()
		{
			return frontier;
		}

		@Override
		public Collection<INode<VAL, LBL>> getExistentialNodes()
		{
			return isExistential ? Collections.singleton(head.getLeaf()) : Collections.emptyList();
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

	public static IPathRule<String, String> fromString(String body, String head) throws ParseException
	{
		return PathRules.create(Paths.pathFromString(body), Paths.pathFromString(head));
	}

	public static IPathRule<String, String> fromString(String body, String head, boolean isExistential) throws ParseException
	{
		return PathRules.create(Paths.pathFromString(body), Paths.pathFromString(head), isExistential);
	}
}