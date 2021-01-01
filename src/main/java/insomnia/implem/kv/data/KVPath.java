package insomnia.implem.kv.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import insomnia.data.AbstractPath;
import insomnia.data.INode;
import insomnia.data.INodeFactory;
import insomnia.data.IPath;
import insomnia.implem.fsa.graphchunk.GraphChunk;
import insomnia.implem.fsa.graphchunk.IGCEdge;
import insomnia.implem.fsa.graphchunk.IGCState;

public class KVPath extends AbstractPath<KVValue, KVLabel>
{
	static INodeFactory<KVValue, KVLabel> nodeFactory;
	static KVLabelFactory                 kvlabelFactory;

	static
	{
		kvlabelFactory = new KVLabelFactory();
	}

	@Override
	public KVValue emptyValue()
	{
		return new KVValue();
	}

	public static List<KVLabel> string2KVLabel(List<String> labels)
	{
		List<KVLabel> kvlabels = new ArrayList<>(labels.size());

		for (String l : labels)
			kvlabels.add(new KVLabel(l));

		return kvlabels;
	}

	// =========================================================================

	public KVPath()
	{
		super(Collections.emptyList());
	}

	// Constructeur de sous-chemin
	public KVPath(IPath<KVValue, KVLabel> path, int begin, int end)
	{
		super(path, begin, end);
	}

	public KVPath(List<KVLabel> labels)
	{
		super(labels);
	}

	public KVPath(List<KVLabel> labels, KVValue value)
	{
		super(labels, value);
	}

	// path doit contenir au moins une clé
	public KVPath(boolean isRooted, List<KVLabel> labels)
	{
		super(isRooted, labels);
	}

	public KVPath(boolean isRooted, List<KVLabel> labels, KVValue value)
	{
		super(isRooted, labels, value);
	}

	public KVPath(boolean isRooted, boolean isTerminal, List<KVLabel> labels)
	{
		super(isRooted, isTerminal, labels);
	}

	// path doit contenir au moins une clé
	public KVPath(boolean isRooted, boolean isTerminal, List<KVLabel> labels, KVValue value)
	{
		super(isRooted, isTerminal, labels, value);
	}

	@Override
	public KVPath subPath(int begin, int end)
	{
		return new KVPath(this, begin, end);
	}

	// =========================================================================

	public static KVPath pathFromString(String p, boolean isRooted, boolean isTerminal, KVValue value)
	{
		return new KVPath(isRooted, isTerminal, string2KVLabel(Arrays.asList(p.trim().split(Pattern.quote(".")))), value);
	}

	/*
	 * Help function for creating paths.
	 * These must be temporary
	 */
	public static KVPath pathFromString(String p)
	{
		return pathFromString(p, new KVValue());
	}

	public static KVPath pathFromString(String p, KVValue value)
	{
		if (p.isEmpty())
			return new KVPath(false, false, Collections.emptyList(), value);

		if (p.equals("^"))
			return new KVPath(true, Collections.emptyList(), value);

		if (p.equals("$"))
			new KVPath(false, true, Collections.emptyList(), value);

		p = p.trim();

		boolean isRooted   = p.charAt(0) == '.';
		boolean isTerminal = p.charAt(p.length() - 1) == '.';

		p = p.substring(isRooted ? 1 : 0, p.length() - (isTerminal ? 1 : 0));
		return pathFromString(p, isRooted, isTerminal, value);
	}

	/**
	 * Get the path validated by a {@link GraphChunk}.
	 * If gchunk does not represent a single path, then return null.
	 * 
	 * @param gchunk
	 * @return
	 */
	public static Optional<KVPath> pathFromGraphChunk(GraphChunk<KVValue, KVLabel> gchunk)
	{
		IGCState<KVValue> state  = gchunk.getStart();
		List<KVLabel>     labels = new ArrayList<>();

		for (;;)
		{
			// TODO: rooted path
			Collection<IGCEdge<KVLabel>> edges = gchunk.getEdges(state);

			int size = edges.size();

			if (size > 1)
				return Optional.empty();

			if (size == 0)
				return Optional.of(new KVPath(labels));

			IGCEdge<KVLabel> edge    = edges.iterator().next();
			Optional<String> label_s = edge.getLabelAsString();

			if (!label_s.isPresent())
				return Optional.empty();

			labels.add(kvlabelFactory.get(label_s.get()));
			state = gchunk.edge_getEnd(edge);
		}
	}

	// =========================================================================

	@Override
	public List<KVEdge> getChildren(INode<KVValue, KVLabel> node)
	{
		assert (node instanceof KVPathNode);
		KVPathNode kvnode = (KVPathNode) node;

		if (kvnode.pos == nbLabels())
			return null;

		int pos = kvnode.pos + 1;
		return Collections.singletonList(new KVEdge(kvnode, new KVPathNode(pos), getLabels().get(pos)));
	}

	@Override
	public KVEdge getParent(INode<KVValue, KVLabel> node)
	{
		assert (node instanceof KVPathNode);
		KVPathNode kvnode = (KVPathNode) node;

		if (kvnode.pos == 0)
			return null;

		int pos = kvnode.pos - 1;
		return new KVEdge(new KVPathNode(pos), kvnode, getLabels().get(pos));
	}

	@Override
	public INode<KVValue, KVLabel> getRoot()
	{
		return new KVPathNode(0);
	}

	@Override
	public IPath<KVValue, KVLabel> setValue(KVValue value)
	{
		return new KVPath(isRooted(), getLabels(), value);
	}
}
