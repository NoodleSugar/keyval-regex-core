package insomnia.implem.data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import insomnia.data.AbstractNode;
import insomnia.data.AbstractPath;
import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.data.ITree;
import insomnia.lib.Side;
import insomnia.lib.help.HelpLists;

final class Path<VAL, LBL> extends AbstractPath<VAL, LBL>
{
	private List<INode<VAL, LBL>> nodes;
	private List<LBL>             labels;

	Path()
	{
		super();
		nodes  = Collections.singletonList(decorate(Nodes.create(), 0));
		labels = Collections.emptyList();
	}

	private Path(ITree<VAL, LBL> parent, INode<VAL, LBL> root)
	{
		this.nodes  = Collections.singletonList(decorate(root, 0));
		this.labels = Collections.emptyList();
	}

	private Path(ITree<VAL, LBL> parent, List<IEdge<VAL, LBL>> edges)
	{
		assert (!edges.isEmpty());

		List<INode<VAL, LBL>> nodes = new ArrayList<>();
		int                   i     = 0;
		for (var node : IEdge.getNodes(edges))
			nodes.add(decorate(node, i++));

		this.nodes  = List.copyOf(nodes);
		this.labels = HelpLists.staticList(IEdge.getLabels(edges));
	}

	private static <VAL, LBL> Path<VAL, LBL> mutable()
	{
		Path<VAL, LBL> ret = new Path<>();
		ret.nodes  = new ArrayList<>();
		ret.labels = new ArrayList<>();
		return ret;
	}

	private void freeze()
	{
		nodes  = List.copyOf(nodes);
		labels = labels.isEmpty() ? Collections.emptyList() : HelpLists.staticList(labels);
	}

	// ==========================================================================

	private static class PNode<VAL, LBL> extends AbstractNode<VAL, LBL>
	{
		INode<VAL, LBL> ref;
		int             index;

		PNode(INode<VAL, LBL> ref, int pos)
		{
			this.ref   = ref;
			this.index = pos;
		}

		@Override
		public Object getID()
		{
			return ref.getID();
		}

		@Override
		public VAL getValue()
		{
			return ref.getValue();
		}

		@Override
		public boolean isRooted()
		{
			return ref.isRooted();
		}

		@Override
		public boolean isTerminal()
		{
			return ref.isTerminal();
		}

		@Override
		public int hashCode()
		{
			return ref.hashCode();
		}

		@Override
		public String toString()
		{
			return new StringBuilder().append(ref).append("@").append(index).toString();
		}

		public int getIndex()
		{
			return index;
		}
	}

	private static <VAL, LBL> PNode<VAL, LBL> decorate(INode<VAL, LBL> node, int index)
	{
		int nindex = getIndex(node);

		if (index == nindex)
			return (PNode<VAL, LBL>) node;

		if (node instanceof PNode<?, ?>)
			node = ((PNode<VAL, LBL>) node).ref;

		return new PNode<>(node, index);
	}

	private static <VAL, LBL> int getIndex(INode<VAL, LBL> node)
	{
		if (!(node instanceof PNode<?, ?>))
			return -1;

		return ((PNode<?, ?>) node).getIndex();
	}

	// ==========================================================================

	static <VAL, LBL> Path<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels)
	{
		return create(isRooted, isTerminal, labels, Collections.emptyList(), 0);
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
	private static <VAL, LBL> Path<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels, List<VAL> values, int valuesOffset)
	{
		Path<VAL, LBL> ret = mutable();
		ret.labels = List.copyOf(labels);
		int                   nbNodes  = labels.size() + 1;
		int                   nbValues = values.size();
		List<INode<VAL, LBL>> nodes    = new ArrayList<>(nbNodes);

		Iterator<VAL> valuesIterator = IntStream.range(0, nbNodes).mapToObj( //
			i -> {
				if (i < valuesOffset)
					return null;
				i -= valuesOffset;
				if (i >= nbValues)
					return null;
				return values.get(i);
			}).iterator();

		if (nbNodes == 1)
			nodes.add(decorate(Nodes.create(isRooted, isTerminal, valuesIterator.next()), 0));
		else
		{
			nodes.add(decorate(Nodes.create(isRooted, false, valuesIterator.next()), 0));

			for (int i = 1; i < nbNodes - 1; i++)
				nodes.add(decorate(Nodes.create(valuesIterator.next()), i));

			nodes.add(decorate(Nodes.create(false, isTerminal, valuesIterator.next()), nbNodes - 1));
		}
		ret.nodes = nodes;
		ret.freeze();
		return ret;
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
	public static <VAL, LBL> Path<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels, List<VAL> values, Side valueSide)
	{
		return create(isRooted, isTerminal, labels, values, valueSide == Side.LEFT ? 0 : labels.size() - values.size() + 1);
	}

	/**
	 * Create a path ended by a value.
	 * 
	 * @param isRooted   the path is rooted
	 * @param isTerminal the path is terminal
	 * @param labels     labels of the path
	 * @param value      the value for the last node
	 */
	public static <VAL, LBL> Path<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels, VAL value)
	{
		return create(isRooted, isTerminal, labels, Collections.singletonList(value), Side.RIGHT);
	}

	// ==========================================================================

	static <VAL, LBL> Path<VAL, LBL> copy(IPath<VAL, LBL> path)
	{
		return copy(path, path.getRoot());
	}

	static <VAL, LBL> Path<VAL, LBL> copy(IPath<VAL, LBL> path, INode<VAL, LBL> root)
	{
		return map(path, root, Nodes::create, Function.identity());
	}

	static <VAL, LBL> Path<VAL, LBL> subPath(IPath<VAL, LBL> path)
	{
		return subPath(path, path.getRoot());
	}

	static <VAL, LBL> Path<VAL, LBL> subPath(IPath<VAL, LBL> path, INode<VAL, LBL> root)
	{
		return map(path, root, Function.identity(), Function.identity());
	}

	static <VAL, LBL> Path<VAL, LBL> subPath(ITree<VAL, LBL> parent, INode<VAL, LBL> root, List<IEdge<VAL, LBL>> edges)
	{
		assert (null != root);
		assert (edges.isEmpty() || edges.get(0).getParent() == root);

		if (edges.isEmpty())
			return emptySubPath(parent, root);

		return new Path<>(parent, edges);
	}

	static <VAL, LBL> IPath<VAL, LBL> subPath(IPath<VAL, LBL> path, int from, int to)
	{
		var        infos  = IPath.subPathInfos(path, from, to);
		RealLimits limits = infos.getLeft();
		var        edges  = path.getEdges();
		var        root   = path.getNodes().get(limits.getFrom());
		return subPath(path, root, edges.subList(limits.getFrom(), limits.getTo()));
	}

	static <VAL, LBL> Path<VAL, LBL> emptySubPath(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		return new Path<>(tree, root);
	}

	// ==========================================================================

	private static <VAL, LBL, TOVAL, TOLBL> Path<TOVAL, TOLBL> map( //
		ITree<VAL, LBL> tree, //
		INode<VAL, LBL> root, //
		Function<INode<VAL, LBL>, INode<TOVAL, TOLBL>> fmapNode, //
		Function<LBL, TOLBL> fmapLabel //
	)
	{
		if (!tree.isPath())
			throw new InvalidParameterException(String.format("%s must be a path", tree));

		Path<TOVAL, TOLBL> ret = mutable();
		ret.nodes.add(decorate(fmapNode.apply(root), 0));

		int i = 1;
		for (var edge : tree.getEdges(root))
		{
			ret.nodes.add(decorate(fmapNode.apply(edge.getChild()), i++));
			ret.labels.add(fmapLabel.apply(edge.getLabel()));
		}
		ret.freeze();
		return ret;
	}

	// =========================================================================

	@Override
	public boolean isPath()
	{
		return true;
	}

	@Override
	public List<LBL> getLabels()
	{
		return labels;
	}

	@Override
	public INode<VAL, LBL> getRoot()
	{
		return nodes.get(0);
	}

	@Override
	public INode<VAL, LBL> getLeaf()
	{
		return nodes.get(nodes.size() - 1);
	}

	@Override
	public List<INode<VAL, LBL>> getNodes()
	{
		return nodes;
	}

	@Override
	public List<INode<VAL, LBL>> getNodes(INode<VAL, LBL> node)
	{
		int index = getIndex(node);
		if (index == -1)
			return Collections.emptyList();
		return nodes.subList(index + 1, nodes.size());
	}

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		int index = getIndex(node);

		if (index < 0 || index >= labels.size())
			return Collections.emptyList();

		return Collections.singletonList(Edges.create(node, nodes.get(index + 1), labels.get(index)));
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		int index = getIndex(node);

		if (index <= 0 || index > labels.size())
			return Optional.empty();

		return Optional.of(Edges.create(nodes.get(index - 1), node, labels.get(index - 1)));
	}
}