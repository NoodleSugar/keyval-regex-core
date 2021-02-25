package insomnia.implem.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import insomnia.data.AbstractPath;
import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.data.PathOp;
import insomnia.data.PathOp.RealLimits;
import insomnia.lib.Side;
import insomnia.lib.help.HelpLists;

final class Path<VAL, LBL> extends AbstractPath<VAL, LBL>
{
	private final List<PathNode> nodes;
	private final List<LBL>      labels;

	/**
	 * Real offset of a node.
	 * Serve in the subPath creation to make sure that the subNodes are the same that the parent path nodes.
	 */
	private int realNodeOffset = 0;

	Path()
	{
		super();
		nodes  = Collections.singletonList(new PathNode(0, null));
		labels = Collections.emptyList();
	}

	Path(IPath<VAL, LBL> src)
	{
		super();
		List<PathNode> tmpNodes = new ArrayList<>();

		int pos = 0;
		for (INode<VAL, LBL> node : src.getNodes())
			tmpNodes.add(new PathNode(pos++, node.getValue()));

		if (src.isRooted())
			tmpNodes.get(0).isRooted = true;

		if (src.isTerminal())
			tmpNodes.get(tmpNodes.size() - 1).isTerminal = true;

		nodes  = HelpLists.staticList(tmpNodes);
		labels = HelpLists.staticList(src.getLabels());
	}

	/**
	 * Construct a path starting with some values.
	 * All remaining nodes are set to a null value.
	 * 
	 * @param isRooted     the path is rooted
	 * @param isTerminal   the path is terminal
	 * @param labels       the labels of the path
	 * @param values       the starting values of the path
	 * @param valuesOffset the node offset from which values will begin to be writen
	 */
	private Path(boolean isRooted, boolean isTerminal, List<LBL> labels, List<VAL> values, int valuesOffset)
	{
		super();
		this.labels = HelpLists.staticList(labels);
		int            nbNodes  = labels.size() + 1;
		int            nbValues = values.size();
		List<PathNode> nodes    = new ArrayList<>(nbNodes);

		Iterator<VAL> valuesIterator = IntStream.range(0, nbNodes).mapToObj( //
			i -> {
				if (i < valuesOffset)
					return null;
				i -= valuesOffset;
				if (i >= nbValues)
					return null;
				return values.get(i);
			}).iterator();

		for (int i = 0; i < nbNodes; i++)
			nodes.add(new PathNode(i, valuesIterator.next()));

		this.nodes = HelpLists.staticList(nodes);

		if (isRooted)
			getRoot().isRooted = true;
		if (isTerminal)
			getLeaf().isTerminal = true;
	}

	private Path(boolean isRooted, boolean isTerminal, List<LBL> labels, List<PathNode> nodes)
	{
		super();
		assert (labels.size() == nodes.size() - 1);
		this.labels = HelpLists.staticList(labels);
		this.nodes  = HelpLists.staticList(nodes);
	}

	/**
	 * Create a {@link Path} with some values added to the begin or the end.
	 * 
	 * @param isRooted   the path is rooted
	 * @param isTerminal the path is terminal
	 * @param labels     labels of the path
	 * @param values     values of the path, can contains more or less elements than needed. If less values are given, remaining values of the path will be replaced by {@code null}.
	 * @param valueSide  the side from which values will be added in order
	 */
	Path(boolean isRooted, boolean isTerminal, List<LBL> labels, List<VAL> values, Side valueSide)
	{
		this(isRooted, isTerminal, labels, values, valueSide == Side.LEFT ? 0 : labels.size() - values.size() + 1);
	}

	/**
	 * Create a path ended by a value.
	 * 
	 * @param isRooted   the path is rooted
	 * @param isTerminal the path is terminal
	 * @param labels     labels of the path
	 * @param value      the value for the last node
	 */
	Path(boolean isRooted, boolean isTerminal, List<LBL> labels, VAL value)
	{
		this(isRooted, isTerminal, labels, Collections.singletonList(value), Side.RIGHT);
	}

	@Override
	public Path<VAL, LBL> subPath(int from, int to)
	{
		assert (from >= 0 && to >= from);

		if (from == to)
			return new Path<>();

		RealLimits limits = PathOp.realLimits(this, from, to);

		Path<VAL, LBL> ret = new Path<>(limits.isRooted(), limits.isTerminal(), labels.subList(limits.getFrom(), limits.getTo()), nodes.subList(limits.getFrom(), limits.getTo() + 1));
		ret.realNodeOffset = limits.getFrom();
		return ret;
	}

	// =========================================================================

	private class PathNode implements INode<VAL, LBL>
	{
		private int pos;

		private boolean isRooted, isTerminal;

		private VAL value;

		PathNode(int pos, VAL value)
		{
			this.pos   = pos;
			this.value = value;
		}

		public int getPos()
		{
			return pos;
		}

		@Override
		public VAL getValue()
		{
			return value;
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
		public String toString()
		{
			return INode.toString(this);
		}
	}

	// =========================================================================

	@Override
	public PathNode getRoot()
	{
		return nodes.get(0);
	}

	@Override
	public PathNode getLeaf()
	{
		return nodes.get(nodes.size() - 1);
	}

	@Override
	public List<LBL> getLabels()
	{
		return labels;
	}

	@Override
	public List<INode<VAL, LBL>> getNodes()
	{
		return HelpLists.downcast(nodes);
	}

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		assert (node instanceof Path.PathNode);
		Path<VAL, LBL>.PathNode pathNode = (Path<VAL, LBL>.PathNode) node;
		int                     pos      = pathNode.getPos() - realNodeOffset + 1;

		if (pos > nbLabels())
			return Collections.emptyList();

		return Collections.singletonList(Edges.create(node, nodes.get(pos), getLabels().get(pos - 1)));
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		assert (node instanceof Path.PathNode);
		Path<VAL, LBL>.PathNode pathNode = (Path<VAL, LBL>.PathNode) node;
		int                     pos      = pathNode.getPos() - realNodeOffset;

		if (pos == 0)
			return Optional.empty();

		return Optional.of(Edges.create(nodes.get(pos), pathNode, getLabels().get(pos)));
	}
}