package insomnia.rule.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;

import insomnia.rule.tree.node.INode;
import insomnia.rule.tree.node.Leaf;
import insomnia.rule.tree.node.Node;
import insomnia.rule.tree.node.Root;

public class Path implements IPath
{
	private List<String> keys;
	private List<INode> nodes;
	private INode root;
	private INode last;

	public Path(String path)
	{
		this(path, false, false);
	}

	public Path(String path, boolean rooted, boolean terminal)
	{
		keys = new ArrayList<>();
		nodes = new ArrayList<>();
		
		String[] path_keys = path.split("\\.");

		for(String k : path_keys)
			keys.add(k);

		if(rooted)
			nodes.add(new Root());
		else
			nodes.add(new Node());

		for(int i = 1; i < keys.size(); i++)
			nodes.add(new Node());

		if(terminal)
			nodes.add(new Leaf());
		else
			nodes.add(new Node());

		root = nodes.get(0);
		last = nodes.get(nodes.size() - 1);
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
		for(String k : keys2)
		{
			if(k.equals(keys.get(index)))
				index++;
			else
				return false;

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
		for(String k : keys2)
		{
			if(k.equals(keys.get(index)))
				index--;
			else
				return false;

			if(index == -1)
				return true;
		}

		return false;
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
