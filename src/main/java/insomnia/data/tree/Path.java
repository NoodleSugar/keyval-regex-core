package insomnia.data.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jws.soap.InitParam;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;

import insomnia.data.tree.node.PathNode;

public class Path implements IPath<String>
{
	public final static boolean default_isRoot     = false;
	public final static boolean default_isTerminal = false;

	private List<String>   labels;
	private List<PathNode> nodes;

	private PathNode root;

	private boolean isRooted;
	private boolean isTerminal;

	public Path subPath(int begin, int end)
	{
		return new Path(this, begin, end);
	}

	// Constructeur de sous-chemin
	public Path(Path path, int begin, int end)
	{
		if (begin == end)
		{
			initPath(this, default_isRoot, default_isTerminal);
			return;
		}
		boolean isRooted   = false;
		boolean isTerminal = false;

		if (path.isRooted)
		{
			if (begin > 0)
			{
				begin--;
				end -= 2;
			}
			else
			{
				isRooted = true;
				end--;
			}
		}

		if (path.isTerminal)
		{
			if (end == path.labels.size() + 1)
			{
				isTerminal = true;
				end--;
			}
		}
		initPath(this, isRooted, isTerminal, path.labels.subList(begin, end).toArray(new String[0]));
	}

	public Path(String... path)
	{
		this(default_isRoot, default_isTerminal, path);
	}

	// path doit contenir au moins une clé
	public Path(boolean isRooted, boolean isTerminal, String... labels)
	{
		initPath(this, isRooted, isTerminal, labels);
	}

	private static void initPath(Path path, boolean isRooted, boolean isTerminal, String... labels)
	{
		path.isRooted   = isRooted;
		path.isTerminal = isTerminal;
		path.nodes      = new ArrayList<>(labels.length + 1);

		// Premier Noeud
		path.root = new PathNode();
		path.nodes.add(path.root);

		/*
		 * Empty path
		 */
		if (labels.length == 0)
		{
			path.labels = Collections.emptyList();
			return;
		}
		path.labels = new ArrayList<>(labels.length);

		String   label;
		PathNode newPathNode;
		PathNode lastPathNode;

		lastPathNode = path.root;

		// Noeuds intermédiaires
		final int n = labels.length;

		for (int i = 0; i < n; i++)
		{
			label = labels[i];
			path.labels.add(label);

			newPathNode = new PathNode(lastPathNode, label);
			path.nodes.add(newPathNode);

			lastPathNode = newPathNode;
		}
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
		return labels.size() + BooleanUtils.toInteger(isRooted) + BooleanUtils.toInteger(isTerminal);
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
		return this.labels.hashCode() + BooleanUtils.toInteger(isRooted) + BooleanUtils.toInteger(isTerminal);
	}

	@Override
	public String toString()
	{
		return (isRooted ? "." : "") + String.join(".", labels) + (isTerminal ? "." : "");
	}
}
