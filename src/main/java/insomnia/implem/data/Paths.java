package insomnia.implem.data;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.IterableUtils;

import insomnia.data.IPath;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.regex.parser.IPRegexElement;
import insomnia.implem.data.regex.parser.PRegexParser;
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
			Collection<IFSAEdge<VAL, LBL>> edges = gfpa.getEdgesOf(state);

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

	static public <VAL, LBL> IPath<VAL, LBL> pathFromPRegexElement(IPRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		return new PathFromPRegexElementBuilder<>(mapValue, mapLabel).create(element);
	}

	static public <VAL, LBL> List<IPath<VAL, LBL>> pathsFromPRegexElement(IPRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		return IterableUtils.toList(new PathsFromPRegexElementBuilder<>(element, mapValue, mapLabel));
	}

	public static IPath<String, String> pathFromString(String p) throws ParseException
	{
		PRegexParser parser = new PRegexParser("''\"\"");
		return Paths.pathFromPRegexElement(parser.parse(p), s -> s, s -> s);
	}

	// =========================================================================

//	private final static IPath<?, ?> emptyPath = new Path<>();

	/**
	 * @return an empty path
	 */
	@SuppressWarnings("unchecked")
	public static <VAL, LBL> IPath<VAL, LBL> empty()
	{
		return (IPath<VAL, LBL>) Trees.empty();
	}

	public static <VAL, LBL> IPath<VAL, LBL> create(IPath<VAL, LBL> src)
	{
		return new Path<>(src);
	}

	/**
	 * Create a path ended by a value.
	 * 
	 * @param isRooted   the path is rooted
	 * @param isTerminal the path is terminal
	 * @param labels     labels of the path
	 * @param value      the value for the last node
	 */
	private static <VAL, LBL> IPath<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels, VAL value)
	{
		return new Path<>(isRooted, isTerminal, labels, value);
	}

	/**
	 * Create a path without value.
	 * 
	 * @param isRooted   the path is rooted
	 * @param isTerminal the path is terminal
	 * @param labels     labels of the path
	 */
	public static <VAL, LBL> IPath<VAL, LBL> create(boolean isRooted, boolean isTerminal, List<LBL> labels)
	{
		return create(isRooted, isTerminal, labels, null);
	}

	/**
	 * Concatenate some paths.
	 * 
	 * @param paths the paths to concatenate in order
	 * @return a path which is the concatenation of all paths
	 * @throws InvalidParameterException
	 */
	public static <VAL, LBL> IPath<VAL, LBL> concat(List<IPath<VAL, LBL>> paths) throws InvalidParameterException
	{
		if (paths.size() == 0)
			return empty();
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
}