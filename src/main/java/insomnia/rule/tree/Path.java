package insomnia.rule.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

import insomnia.rule.tree.node.PathNode;

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
		if (path.isRooted)
		{
			if (begin > 0)
			{
				begin--;
				end -= 2;
			}
			else
			{
				this.isRooted = true;
				end--;
			}
		}

		if (path.isTerminal)
		{
			if(end == path.labels.size() + 1)
			{
				this.isTerminal = true;
				end--;
			}
		}
		labels = path.labels.subList(begin, end);
		nodes  = path.nodes.subList(begin, end + 1);
		root   = path.nodes.get(begin);
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
		nodes           = new ArrayList<>(path.length + 1);

		// Premier Noeud
		root = new PathNode();
		nodes.add(root);

		/*
		 * Empty path
		 */
		if (path.length == 0)
		{
			labels = Collections.emptyList();
			return;
		}
		labels = new ArrayList<>(path.length);

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

		return Paths.areSimplyEquals(this, (IPath<?>) o);
	}

	@Override
	public int hashCode()
	{
		return this.labels.hashCode();
	}

	@Override
	public String toString()
	{
		return (isRooted ? "." : "") + String.join(".", labels) + (isTerminal ? "." : "");
	}
}
