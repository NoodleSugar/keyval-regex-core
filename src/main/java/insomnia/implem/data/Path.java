package insomnia.implem.data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import insomnia.data.AbstractPath;
import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.PathOp;
import insomnia.data.PathOp.RealLimits;

final class Path<VAL, LBL> extends AbstractPath<VAL, LBL>
{
	/**
	 * Real offset of a node.
	 * Serve in the subPath creation to make sure that the subNodes are the same that the parent path nodes.
	 */
	private int realNodeOffset = 0;

	Path()
	{
		super();
	}

	// path doit contenir au moins une clé
	Path(int offset, boolean isRooted, boolean isTerminal, List<LBL> labels, VAL value)
	{
		super(isRooted, isTerminal, labels, value);
		this.realNodeOffset = offset;
	}

	@Override
	public Path<VAL, LBL> subPath(int from, int to)
	{
		if (from == to)
			return new Path<>();

		RealLimits limits = PathOp.realLimits(this, from, to);
		VAL        value;

		if (limits.isTerminal())
			value = getValue().orElse(null);
		else
			value = null;

		return new Path<>(limits.getFrom() + realNodeOffset, limits.isRooted(), limits.isTerminal(), getLabels().subList(limits.getFrom(), limits.getTo()), value);
	}

	// =========================================================================

	private static class PathNode<VAL, LBL> implements INode<VAL, LBL>
	{
		private int pos;

		private boolean isRooted, isTerminal;

		private Optional<VAL> value;

		PathNode(int pos, boolean isRooted, boolean isTerminal, Optional<VAL> value)
		{
			this.pos   = pos;
			this.value = value;
		}

		@Override
		public Optional<VAL> getValue()
		{
			return value;
		}

		int getPos()
		{
			return pos;
		}

		@Override
		public boolean isRooted()
		{
			return isRooted;
		}

		@Override
		public boolean isTerminal()
		{
			return isTerminal;
		}

		@Override
		public int hashCode()
		{
			return pos;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof PathNode))
				return false;

			@SuppressWarnings("unchecked")
			PathNode<VAL, LBL> node = (PathNode<VAL, LBL>) obj;
			return pos == node.pos;
		}

		@Override
		public String toString()
		{
			return "<" + pos + ">";
		}
	}

	// =========================================================================

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		assert (node instanceof PathNode);
		PathNode<VAL, LBL> pathNode = (PathNode<VAL, LBL>) node;

		if (pathNode.getPos() == nbLabels())
			return Collections.emptyList();

		int     pos        = pathNode.getPos();
		boolean lastPos    = pos == nbLabels() - 1;
		boolean isTerminal = lastPos && isTerminal();

		Optional<VAL> value = lastPos ? getValue() : Optional.empty();
		return Collections.singletonList(Edges.create(pathNode, new PathNode<>(pos + 1, false, isTerminal, value), getLabels().get(pos)));
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		assert (node instanceof PathNode);
		PathNode<VAL, LBL> pathNode = (PathNode<VAL, LBL>) node;

		if (pathNode.getPos() == 0)
			return Optional.empty();

		int     pos      = pathNode.getPos() - 1;
		boolean isRooted = pos == 0 && isRooted();
		return Optional.of(Edges.create(new PathNode<>(pos, isRooted, false, Optional.empty()), pathNode, getLabels().get(pos)));
	}

	@Override
	public INode<VAL, LBL> getRoot()
	{
		boolean isTerminal = isEmpty() && isTerminal();
		return new PathNode<>(realNodeOffset, isRooted(), isTerminal, Optional.empty());
	}
}