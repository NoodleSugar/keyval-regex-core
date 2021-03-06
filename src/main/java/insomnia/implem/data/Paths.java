package insomnia.implem.data;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;

import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.data.ITree;
import insomnia.fsa.IFSAEdge;
import insomnia.fsa.IFSAState;
import insomnia.fsa.fpa.IGFPA;
import insomnia.implem.data.regex.parser.IRegexElement;
import insomnia.implem.fsa.fpa.graphchunk.GraphChunk;
import insomnia.implem.fsa.labelcondition.FSALabelConditions;
import insomnia.implem.fsa.valuecondition.FSAValueConditions;

public final class Paths
{
	private Paths()
	{
		throw new AssertionError();
	}

	// ==========================================================================

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

		var     rootedCond   = FSAValueConditions.createEq(Trees.getRootValue());
		var     terminalCond = FSAValueConditions.createEq(Trees.getTerminalValue());
		boolean isRooted     = false, isTerminal = false;

		for (boolean dofirst = true;; dofirst = false)
		{
			Collection<IFSAEdge<VAL, LBL>> edges = gfpa.getEdgesOf(state);

			int size = edges.size();

			if (size > 1)
				return null;

			if (size == 0)
				return create(isRooted, isTerminal, labels);

			IFSAEdge<VAL, LBL> edge      = edges.iterator().next();
			var                nextState = edge.getChild();

			if (dofirst)
			{
				if (rootedCond.equals(state.getValueCondition()))
				{
					assert FSALabelConditions.isAny(edge.getLabelCondition());
					isRooted = true;
					state    = nextState;
					continue;
				}
			}
			else if (terminalCond.equals(nextState.getValueCondition()))
			{
				assert FSALabelConditions.isAny(edge.getLabelCondition());
				assert gfpa.getEdgesOf(nextState).size() == 0;
				isTerminal = true;
				state      = nextState;
				continue;
			}
			Collection<LBL> validLabels = edge.getLabelCondition().getLabels();

			if (validLabels.size() != 1)
				return null;

			labels.add(validLabels.iterator().next());
			state = nextState;
		}
	}

	static public <VAL, LBL> IPath<VAL, LBL> pathFromPRegexElement(IRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		if (!element.isPath())
			throw new InvalidParameterException(String.format("'%s' does not represent a path", element));
		return (IPath<VAL, LBL>) Trees.treeFromPRegexElement(element, mapValue, mapLabel);
	}

	static public <VAL, LBL> List<IPath<VAL, LBL>> pathsFromPRegexElement(IRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		if (!element.isPath())
			throw new InvalidParameterException(String.format("'%s' does not represent paths", element));
		return CollectionUtils.collect(new TreesFromPRegexElementBuilder<>(element, mapValue, mapLabel), e -> (IPath<VAL, LBL>) e, new ArrayList<>(element.size()));
	}

	static public IPath<String, String> pathFromPRegexElement(IRegexElement element)
	{
		return pathFromPRegexElement(element, Function.identity(), Function.identity());
	}

	static public List<IPath<String, String>> pathsFromPRegexElement(IRegexElement element)
	{
		return pathsFromPRegexElement(element, Function.identity(), Function.identity());
	}

	public static IPath<String, String> pathFromString(String p) throws ParseException
	{
		return pathFromPRegexElement(Trees.getParser().parse(p), Function.identity(), Function.identity());
	}

	public static List<IPath<String, String>> pathsFromString(String p) throws ParseException
	{
		return pathsFromPRegexElement(Trees.getParser().parse(p), Function.identity(), Function.identity());
	}

	// ==========================================================================

	/**
	 * @return an empty path
	 */
	@SuppressWarnings("unchecked")
	public static <VAL, LBL> IPath<VAL, LBL> empty()
	{
		return (IPath<VAL, LBL>) Trees.empty();
	}

	public static <VAL, LBL> IPath<VAL, LBL> emptySubPath(ITree<VAL, LBL> path, INode<VAL, LBL> root)
	{
		return Path.emptySubPath(path, root);
	}

	// ==========================================================================

	public static <VAL, LBL> IPath<VAL, LBL> create(IPath<VAL, LBL> src)
	{
		return Path.copy(src);
	}

	public static <VAL, LBL> IPath<VAL, LBL> create(IPath<VAL, LBL> src, int from, int to)
	{
		return create(subPath(src, from, to));
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
		return Path.create(isRooted, isTerminal, labels);
	}

	// ==========================================================================

	public static <VAL, LBL> IPath<VAL, LBL> subPath(IPath<VAL, LBL> path, int from, int to)
	{
		return Path.subPath(path, from, to);
	}

	/**
	 * View the tree as a sub-path.
	 * <p>
	 * The returned path is a snapshot of the sub-path.
	 * 
	 * @param tree the parent tree
	 * @return the selected sub-path
	 * @throws IllegalArgumentException if the tree does not represent a tree
	 */
	public static <VAL, LBL> IPath<VAL, LBL> subPath(ITree<VAL, LBL> tree)
	{
		return subPath(tree, tree.getRoot());
	}

	/**
	 * Get a sub-path of the tree.
	 * <p>
	 * The returned path is a snapshot of the sub-path.
	 * 
	 * @param tree the parent tree
	 * @param root the root of the sub-path
	 * @return the selected sub-path
	 * @throws IllegalArgumentException if the sub-tree does not represent a tree
	 */
	public static <VAL, LBL> IPath<VAL, LBL> subPath(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		if (!tree.isPath())
			throw new IllegalArgumentException();

		return Path.subPath(tree, root, tree.getEdges(root));
	}

	// ==========================================================================

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
