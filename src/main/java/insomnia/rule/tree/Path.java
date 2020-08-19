package insomnia.rule.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import insomnia.rule.tree.node.PathNode;

public class Path implements IPath<String>
{
	public final static boolean default_isRoot     = false;
	public final static boolean default_isTerminal = false;

	private List<String>   labels;
	private List<PathNode> nodes;

	private PathNode root;
//	private PathNode last;

	private boolean isRooted;
	private boolean isTerminal;

	// Constructeur de sous-chemin
	public Path(Path path, int begin, int end)
	{
		labels = path.labels.subList(begin, end);
		nodes  = path.nodes.subList(begin, end + 1);
		root   = path.nodes.get(begin);
//		last = path.nodes.get(end);
	}

	public Path(String... path)
	{
		this(default_isRoot, default_isTerminal, path);
	}

	// path doit contenir au moins une clé
	public Path(boolean isRooted, boolean isTerminal, String... path)
	{
		this.isRooted   = isRooted;
		this.isTerminal = isTerminal;

		// Premier Noeud
		root = new PathNode();
		nodes.add(root);

		/*
		 * Empty path
		 */
		if (path.length == 0)
		{
			labels = Collections.emptyList();
			nodes  = Collections.emptyList();
			return;
		}
		labels = new ArrayList<>(path.length);
		nodes  = new ArrayList<>(path.length);

		String   label;
		PathNode newPathNode;
		PathNode lastPathNode;

		lastPathNode = root;

		// Noeuds intermédiaires
		final int n = path.length;

		for (int i = 0; i < n; i++)
		{
			label = path[i];
			labels.add(label);

			newPathNode = new PathNode(lastPathNode, label);
			nodes.add(newPathNode);

			lastPathNode = newPathNode;
		}
//		last = lastPathNode;
	}

	@Override
	public boolean isEmpty()
	{
		return nodes.isEmpty();
	}

	@Override
	public PathNode getRoot()
	{
		return root;
	}

	@Override
	public List<String> getLabels()
	{
		return labels;
	}

	@Override
	public int size()
	{
		return labels.size();
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
	public boolean isIncluded(IPath<String> path)
	{
		return Paths.isIncluded(this, path);
	}

	@Override
	public boolean isPrefix(IPath<String> path)
	{
		return Paths.isPrefix(this, path);
	}

	@Override
	public boolean isSuffix(IPath<String> path)
	{
		return Paths.isSuffix(this, path);
	}

	@Override
	public boolean isEqual(IPath<String> path)
	{
		if (labels.size() != path.getLabels().size())
			return false;

		return isPrefix(path);
	}

	@Override
	public boolean hasPrefixInSuffix(IPath<String> path)
	{
		return Paths.hasPrefixInSuffix(this, path);
	}

	// =========================================================================
	// Object Override

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof IPath))
			return false;

		return Paths.areEquals(this, (IPath<?>) o);
	}

	@Override
	public int hashCode()
	{
		return this.labels.hashCode();
	}

	@Override
	public String toString()
	{
		return String.join(".", labels);
	}
}
