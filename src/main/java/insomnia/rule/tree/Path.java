package insomnia.rule.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;

import insomnia.rule.tree.edge.IEdge;
import insomnia.rule.tree.node.INode;
import insomnia.rule.tree.node.IPathNode;

public class Path implements IPath
{
	public class Edge implements IEdge
	{
		private String key;
		private INode parent;
		private INode child;

		public Edge(INode parent, INode child, String key)
		{
			this.key = key;
			this.parent = parent;
			this.child = child;
		}

		@Override
		public String getKey()
		{
			return key;
		}

		@Override
		public INode getParent()
		{
			return parent;
		}

		@Override
		public INode getChild()
		{
			return child;
		}
	}

	public class Node implements IPathNode
	{
		private IEdge parent;
		private IEdge child;

		private Node()
		{
			parent = null;
			child = null;
		}

		private Node(Node parent, String key)
		{
			this.parent = new Edge(parent, this, key);
			child = null;
		}

		private void setChild(Node child)
		{
			this.child = child.parent;
		}

		@Override
		public boolean isRoot()
		{
			return false;
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}

		@Override
		public List<IEdge> getParents()
		{
			List<IEdge> p = new ArrayList<>();
			p.add(parent);
			return p;
		}

		@Override
		public List<IEdge> getChildren()
		{
			List<IEdge> c = new ArrayList<>();
			c.add(child);
			return c;
		}

		@Override
		public IEdge getParent()
		{
			return parent;
		}

		@Override
		public IEdge getChild()
		{
			return child;
		}
	}

	public class Leaf extends Node
	{
		public Leaf(Node parent, String key)
		{
			super(parent, key);
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}
	}

	public class Root extends Node
	{
		public Root()
		{
			super();
		}

		@Override
		public boolean isRoot()
		{
			return true;
		}
	}

	private List<String> keys;
	private List<INode> nodes;
	private INode root;
	private INode last;

	// Constructeur de sous-chemin
	public Path(Path path, int begin, int end)
	{
		keys = path.keys.subList(begin, end);
		nodes = path.nodes.subList(begin, end + 1);
		root = path.nodes.get(begin);
		last = path.nodes.get(end + 1);
	}

	public Path(String path)
	{
		this(path, false, false);
	}

	// path doit contenir au moins une clé
	public Path(String path, boolean rooted, boolean terminal)
	{
		keys = new ArrayList<>();
		nodes = new ArrayList<>();

		String[] path_keys = path.split("\\.");
		String key;
		Node newNode;
		Node lastNode;

		// Premier Noeud
		if(rooted)
			lastNode = new Root();
		else
			lastNode = new Node();
		nodes.add(lastNode);
		root = lastNode;

		// Noeuds intermédiaires
		int n = path_keys.length;
		for(int i = 0; i < n - 1; i++)
		{
			key = path_keys[i];
			keys.add(key);

			newNode = new Node(lastNode, key);
			lastNode.setChild(newNode);

			nodes.add(newNode);
			lastNode = newNode;
		}

		// Dernier noeud
		key = path_keys[n - 1];
		keys.add(key);
		if(terminal)
			newNode = new Leaf(lastNode, key);
		else
			newNode = new Node(lastNode, key);
		lastNode.setChild(newNode);
		nodes.add(newNode);
		last = lastNode;
	}

	@Override
	public INode getRoot()
	{
		return root;
	}

	@Override
	public INode getParent(INode node) throws TreeException
	{
		if(node == root)
			return null;

		int index = nodes.indexOf(node);
		if(index == -1)
			throw new TreeException("This node is not in the path");

		return nodes.get(index - 1);
	}

	@Override
	// TODO modifier
	public List<Entry<String, INode>> getChildren(INode node) throws TreeException
	{
		int index = nodes.indexOf(node);
		if(index == -1)
			throw new TreeException("This node is not in the path");

		if(index == nodes.size() - 1)
			return null;

		String k = keys.get(index);
		INode n = nodes.get(index + 1);
		Entry<String, INode> p = new ImmutablePair<String, INode>(k, n);
		ArrayList<Entry<String, INode>> a = new ArrayList<>();
		a.add(p);

		return a;
	}

	@Override
	public List<String> getKeys()
	{
		return keys;
	}

	@Override
	public int getSize()
	{
		return keys.size();
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
	public boolean isIncluded(IPath path)
	{
		List<String> keys2 = path.getKeys();

		if(keys.size() > keys2.size())
			return false;

		int index = 0;
		// Pour chaque clé de keys2
		for(String k : keys2)
		{
			// Si k et la clé de keys à l'index sont égales
			if(k.equals(keys.get(index)))
				// Incrémentation de l'index de keys
				index++;
			// Sinon remise à 0 de l'index
			else
				index = 0;

			// Si la dernière clé a été validée
			if(index == keys.size())
				return true;
		}
		return false;
	}

	@Override
	public boolean isPrefix(IPath path)
	{
		List<String> keys2 = path.getKeys();

		if(keys.size() > keys2.size())
			return false;

		int index = 0;
		// Pour chaque clé de keys2
		for(String k : keys2)
		{
			// Si k et la clé de keys à l'index sont égales
			if(k.equals(keys.get(index)))
				// Incrémentation de l'index de keys
				index++;
			// Sinon ce n'est pas un préfixe
			else
				return false;

			// Si la dernière clé a été validée
			if(index == keys.size())
				return true;
		}

		return false;
	}

	@Override
	public boolean isSuffix(IPath path)
	{
		List<String> keys2 = new ArrayList<>();
		keys2.addAll(path.getKeys());
		Collections.reverse(keys2);

		if(keys.size() > keys2.size())
			return false;

		int index = keys.size() - 1;
		// Pour chaque clé de keys2
		for(String k : keys2)
		{
			// Si k et la clé de keys à l'index sont égales
			if(k.equals(keys.get(index)))
				// Décrémentation de l'index de keys
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
	public boolean isEqual(IPath path)
	{
		if(keys.size() != path.getKeys().size())
			return false;

		return isPrefix(path);
	}

	@Override
	public boolean hasPrefixInSuffix(IPath path)
	{
		StringBuffer prefix = new StringBuffer();
		StringBuffer suffix = new StringBuffer();

		List<String> keys2 = path.getKeys();
		int n = keys2.size();
		int min = Math.min(n, keys.size());
		int i = 0;

		while(i < min)
		{
			prefix.append(keys.get(i));
			suffix.insert(0, keys2.get(n - i - 1));
			i++;
			if(prefix.toString().equals(suffix.toString()))
				return true;
		}

		return false;
	}
}
