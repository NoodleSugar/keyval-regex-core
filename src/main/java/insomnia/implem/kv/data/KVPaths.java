package insomnia.implem.kv.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import insomnia.implem.fsa.graphchunk.GraphChunk;
import insomnia.implem.fsa.graphchunk.IGCEdge;
import insomnia.implem.fsa.graphchunk.IGCState;

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
