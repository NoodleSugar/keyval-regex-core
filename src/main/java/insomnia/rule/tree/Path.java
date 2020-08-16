package insomnia.rule.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import insomnia.rule.tree.edge.Edge;
import insomnia.rule.tree.node.INode;
import insomnia.rule.tree.node.PathNode;

public class Path implements IPath<String>
{
	private List<String> labels;
	private List<PathNode> nodes;
	private PathNode root;
	private PathNode last;

	// Constructeur de sous-chemin
	public Path(Path path, int begin, int end)
	{
		labels = path.labels.subList(begin, end);
		nodes = path.nodes.subList(begin, end + 1);
		root = path.nodes.get(begin);
		last = path.nodes.get(end);
	}

	public Path(String path)
	{
		this(path, false, false);
	}

	// path doit contenir au moins une clé
	public Path(String path, boolean rooted, boolean terminal)
	{
		labels = new ArrayList<>();
		nodes = new ArrayList<>();

		String[] path_labels = path.split("\\.");
		String label;
		PathNode newPathNode;
		PathNode lastPathNode;

		// Premier Noeud
		lastPathNode = new PathNode(null, null, rooted, false);
		nodes.add(lastPathNode);
		root = lastPathNode;

		// Noeuds intermédiaires
		int n = path_labels.length;
		for(int i = 0; i < n - 1; i++)
		{
			label = path_labels[i];
			labels.add(label);

			newPathNode = new PathNode(lastPathNode, label);
			lastPathNode.setChild(newPathNode);

			nodes.add(newPathNode);
			lastPathNode = newPathNode;
		}

		// Dernier noeud
		label = path_labels[n - 1];
		labels.add(label);

		last = new PathNode(lastPathNode, label, false, terminal);
		lastPathNode.setChild(last);
		nodes.add(last);
	}

	@Override
	public PathNode getRoot()
	{
		return root;
	}

	/*
	 * node doit appartenir au chemin
	 */
	@Override
	public Edge getParent(INode<String> node)
	{
		return ((PathNode) node).getParent();
	}

	/*
	 * node doit appartenir au chemin
	 */
	@Override
	public Edge getChild(INode<String> node)
	{
		return ((PathNode) node).getChild();
	}

	/*
	 * node doit appartenir au chemin
	 */
	@Override
	public List<Edge> getChildren(INode<String> node)
	{
		ArrayList<Edge> a = new ArrayList<>();
		a.add(getChild(node));

		return a;
	}

	@Override
	public List<String> getLabels()
	{
		return labels;
	}

	@Override
	public int getSize()
	{
		return labels.size();
	}

	@Override
	public boolean isRooted()
	{
		return root.isRoot();
	}

	@Override
	public boolean isTerminal()
	{
		return last.isLeaf();
	}

	@Override
	public boolean isIncluded(IPath<String> path)
	{
		List<String> labels2 = path.getLabels();

		if(labels.size() > labels2.size())
			return false;

		int index = 0;
		// Pour chaque clé de labels2
		for(String k : labels2)
		{
			// Si k et la clé de labels à l'index sont égales
			if(k.equals(labels.get(index)))
				// Incrémentation de l'index de labels
				index++;
			// Sinon remise à 0 de l'index
			else
				index = 0;

			// Si la dernière clé a été validée
			if(index == labels.size())
				return true;
		}
		return false;
	}

	@Override
	public boolean isPrefix(IPath<String> path)
	{
		List<String> labels2 = path.getLabels();

		if(labels.size() > labels2.size())
			return false;

		int index = 0;
		// Pour chaque clé de labels2
		for(String k : labels2)
		{
			// Si k et la clé de labels à l'index sont égales
			if(k.equals(labels.get(index)))
				// Incrémentation de l'index de labels
				index++;
			// Sinon ce n'est pas un préfixe
			else
				return false;

			// Si la dernière clé a été validée
			if(index == labels.size())
				return true;
		}

		return false;
	}

	@Override
	public boolean isSuffix(IPath<String> path)
	{
		List<String> labels2 = new ArrayList<>();
		labels2.addAll(path.getLabels());
		Collections.reverse(labels2);

		if(labels.size() > labels2.size())
			return false;

		int index = labels.size() - 1;
		// Pour chaque clé de labels2
		for(String k : labels2)
		{
			// Si k et la clé de labels à l'index sont égales
			if(k.equals(labels.get(index)))
				// Décrémentation de l'index de labels
				index--;
			// Sinon ce n'est pas un suffixe
			else
				return false;

			// Si la dernière clé a été validée
			if(index == -1)
				return true;
		}

		return false;
	}

	@Override
	public boolean isEqual(IPath<String> path)
	{
		if(labels.size() != path.getLabels().size())
			return false;

		return isPrefix(path);
	}

	@Override
	public boolean hasPrefixInSuffix(IPath<String> path)
	{
		StringBuffer prefix = new StringBuffer();
		StringBuffer suffix = new StringBuffer();

		List<String> labels2 = path.getLabels();
		int n = labels2.size();
		int min = Math.min(n, labels.size());
		int i = 0;

		while(i < min)
		{
			prefix.append(labels.get(i));
			suffix.insert(0, labels2.get(n - i - 1));
			i++;
			if(prefix.toString().equals(suffix.toString()))
				return true;
		}

		return false;
	}
}
