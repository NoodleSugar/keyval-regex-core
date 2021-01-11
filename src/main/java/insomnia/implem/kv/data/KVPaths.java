package insomnia.implem.kv.data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import insomnia.data.IPath;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.fpa.graphchunk.IGCEdge;
import insomnia.implem.fsa.fpa.graphchunk.IGCState;

public final class KVPaths
{
	private KVPaths()
	{
	}

	public static List<KVLabel> string2KVLabel(List<String> labels)
	{
		List<KVLabel> kvlabels = new ArrayList<>(labels.size());

		for (String l : labels)
			kvlabels.add(new KVLabel(l));

		return kvlabels;
	}

	// =========================================================================

	public static KVPath create()
	{
		return new KVPath();
	}

	private static KVPath create(boolean isRooted, boolean isTerminal, List<KVLabel> labels, KVValue value)
	{
		return new KVPath(isRooted, isTerminal, labels, value);
	}

	public static KVPath create(boolean isRooted, boolean isTerminal, List<KVLabel> labels)
	{
		return create(isRooted, isTerminal, labels, null);
	}

	/**
	 * if null != value the path will be terminal.
	 */
	public static KVPath create(boolean isRooted, List<KVLabel> labels, KVValue value)
	{
		return create(isRooted, null != value, labels, null);
	}

	public static KVPath concat(IPath<KVValue, KVLabel>... paths) throws InvalidParameterException
	{
		if (paths.length == 0)
			return KVPaths.create();
		if (paths.length == 1)
			return KVPaths.create(paths[0].isRooted(), paths[0].isTerminal(), paths[0].getLabels());

		if (paths[0].isTerminal() || paths[paths.length - 1].isRooted())
			throw new InvalidParameterException();

		for (int i = 1, c = paths.length - 1; i < c; i++)
		{
			if (paths[i].isRooted() || paths[i].isTerminal())
				throw new InvalidParameterException();
		}
		List<KVLabel> labels     = new ArrayList<>();
		boolean       isRooted   = paths[0].isRooted();
		boolean       isTerminal = paths[paths.length - 1].isTerminal();

		for (IPath<KVValue, KVLabel> path : Arrays.asList(paths))
			labels.addAll(path.getLabels());

		return create(isRooted, isTerminal, labels);
	}

	// =========================================================================

	private static KVPath pathFromString(String p, boolean isRooted, boolean isTerminal, KVValue value)
	{
		return create(isRooted, isTerminal, string2KVLabel(Arrays.asList(p.trim().split(Pattern.quote(".")))), value);
	}

	/*
	 * Help function for creating paths.
	 * These must be temporary
	 */
	public static KVPath pathFromString(String p)
	{
		return pathFromString(p, null);
	}

	/*
	 * If value != null the resulting path is automatically terminal.
	 */
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
		return pathFromString(p, isRooted, isTerminal || null != value, value);
	}

	/**
	 * Get the path validated by a {@link GraphChunk}.
	 * If gchunk does not represent a single path, then return null.
	 * The loop edges are avoided.
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
				return Optional.of(create(false, false, labels));

			IGCEdge<KVLabel> edge    = edges.iterator().next();
			Optional<String> label_s = edge.getLabelAsString();

			if (!label_s.isPresent())
				return Optional.empty();

			labels.add(KVLabels.create(label_s.get()));
			state = gchunk.edge_getEnd(edge);
		}
	}
}
