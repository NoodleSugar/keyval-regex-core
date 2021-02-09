package insomnia.implem.data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import insomnia.data.IPath;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;

public final class Paths
{
	private Paths()
	{
		throw new AssertionError();
	}

	// =========================================================================

	/**
	 * Get the paths validated by a {@link GraphChunk}.
	 * If gchunk does not represent a single path, then return null.
	 * The loop edges are avoided.
	 * 
	 * @param gchunk
	 * @return
	 */
	public static <VAL, LBL> IPath<VAL, LBL> pathFromGFPA(IGFPA<VAL, LBL> gfpa)
	{
		Collection<IFSAState<VAL, LBL>> states = gfpa.getInitialStates();

		if (states.size() != 1)
			return null;

		IFSAState<VAL, LBL> state  = states.iterator().next();
		List<LBL>           labels = new ArrayList<>();

		for (;;)
		{
//			// TODO: rooted path
			Collection<IFSAEdge<VAL, LBL>> edges = gfpa.getEdges(state);

			int size = edges.size();

			if (size > 1)
				return null;

			if (size == 0)
				return create(false, false, labels);

			IFSAEdge<VAL, LBL> edge        = edges.iterator().next();
			Collection<LBL>    validLabels = edge.getLabelCondition().getLabels();

			if (validLabels.size() != 1)
				return null;

			labels.add(validLabels.iterator().next());
			state = edge.getChild();
		}
	}

	// =========================================================================

	public static <VAL, LBL> IPath<VAL, LBL> create()
	{
		return new Path<>();
	}

	private static <VAL, LBL> IPath<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels, VAL value)
	{
		return new Path<>(0, isRooted, isTerminal, labels, value);
	}

	public static <VAL, LBL> IPath<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels)
	{
		return create(isRooted, isTerminal, labels, null);
	}

	public static <VAL, LBL> IPath<VAL, LBL> concat(List<IPath<VAL, LBL>> paths) throws InvalidParameterException
	{
		if (paths.size() == 0)
			return create();
		if (paths.size() == 1)
			return create(paths.get(0).isRooted(), paths.get(0).isTerminal(), paths.get(0).getLabels());

		if (paths.get(0).isTerminal() || paths.get(paths.size() - 1).isRooted())
			throw new InvalidParameterException();

		for (int i = 1, c = paths.size() - 1; i < c; i++)
		{
			if (paths.get(i).isRooted() || paths.get(i).isTerminal())
				throw new InvalidParameterException();
		}
		List<LBL> labels     = new ArrayList<>();
		boolean   isRooted   = paths.get(0).isRooted();
		boolean   isTerminal = paths.get(paths.size() - 1).isTerminal();

		for (IPath<VAL, LBL> path : paths)
			labels.addAll(path.getLabels());

		return create(isRooted, isTerminal, labels);
	}

	// =========================================================================

	public static <LBL> List<LBL> string2KVLabel(List<String> labels, Function<String, LBL> labelFactory)
	{
		List<LBL> kvlabels = new ArrayList<>(labels.size());

		for (String l : labels)
			kvlabels.add(labelFactory.apply(l));

		return kvlabels;
	}

	public static <VAL, LBL> IPath<VAL, LBL> pathFromString(String p, boolean isRooted, boolean isTerminal, VAL value, Function<String, LBL> labelFactory)
	{
		return create(isRooted, isTerminal, string2KVLabel(Arrays.asList(p.trim().split(Pattern.quote("."))), labelFactory), value);
	}

	/*
	 * Help function for creating paths.
	 * These must be temporary
	 */
	public static <VAL, LBL> IPath<VAL, LBL> pathFromString(String p, Function<String, LBL> labelFactory)
	{
		return pathFromString(p, null, labelFactory);
	}

	/*
	 * If value != null the resulting path is automatically terminal.
	 */
	public static <VAL, LBL> IPath<VAL, LBL> pathFromString(String p, VAL value, Function<String, LBL> labelFactory)
	{
		if (p.isEmpty())
			return create(false, false, Collections.emptyList(), value);

		if (p.equals("^"))
			return create(true, false, Collections.emptyList(), value);

		if (p.equals("$"))
			return create(false, true, Collections.emptyList(), value);

		p = p.trim();

		boolean isRooted   = p.charAt(0) == '.';
		boolean isTerminal = p.charAt(p.length() - 1) == '.';

		p = p.substring(isRooted ? 1 : 0, p.length() - (isTerminal ? 1 : 0));
		return pathFromString(p, isRooted, isTerminal || null != value, value, labelFactory);
	}
}
