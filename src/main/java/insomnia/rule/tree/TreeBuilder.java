package insomnia.rule.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import insomnia.rule.tree.value.Value;

public class TreeBuilder
{
	protected class EdgeData
	{
		public int child;
		public String label;

		public EdgeData(int c, String l)
		{
			child = c;
			label = l;
		}
	}

	protected boolean isRooted;
	protected HashMap<Integer, Value> leaves;
	protected HashMap<Integer, ArrayList<EdgeData>> edges;
	private ArrayDeque<Integer> nodesQueue;
	private int currentNode;
	private int nextFreeNode;

	public TreeBuilder(boolean rooted)
	{
		isRooted = rooted;
		edges = new HashMap<>();
		nodesQueue = new ArrayDeque<>();
		currentNode = 0;
		nextFreeNode = 1;

		edges.put(0, new ArrayList<>());
	}

	public TreeBuilder addLeaf(String label, Double number)
	{
		edges.get(currentNode).add(new EdgeData(nextFreeNode, label));
		leaves.put(nextFreeNode++, new Value(number));
		return this;
	}
	
	public TreeBuilder addLeaf(String label, String string)
	{
		edges.get(currentNode).add(new EdgeData(nextFreeNode, label));
		leaves.put(nextFreeNode++, new Value(string));
		return this;
	}
	
	public TreeBuilder addChild(String label)
	{
		edges.get(currentNode).add(new EdgeData(nextFreeNode, label));
		edges.put(nextFreeNode++, new ArrayList<>());
		return this;
	}

	/*
	 * The child must not be a leaf
	 */
	public TreeBuilder goToChild(int number)
	{
		nodesQueue.addFirst(currentNode);
		currentNode = edges.get(currentNode).get(number).child;
		return this;
	}

	public TreeBuilder backToParent()
	{
		currentNode = nodesQueue.pop();
		return this;
	}

	public TreeBuilder backToRoot()
	{
		nodesQueue.clear();
		currentNode = 0;
		return this;
	}

	public Tree build()
	{
		return new Tree(this);
	}
}
