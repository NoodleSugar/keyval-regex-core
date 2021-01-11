package insomnia.implem.data.factory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.factory.ITreeBuilder;
import insomnia.implem.data.Trees;

/**
 * An implementation of a tree builder.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public final class TreeBuilder<VAL, LBL> implements ITreeBuilder<VAL, LBL>
{
	private class Node implements INode<VAL, LBL>
	{
		private Optional<VAL> value;

		Edge                  parent   = null;
		List<IEdge<VAL, LBL>> children = new ArrayList<>();

		boolean isRooted   = false;
		boolean isTerminal = false;

		Node()
		{
			this.value = Optional.empty();
		}

		void addEdge(Edge edge)
		{
			children.add(edge);
		}

		void setParent(Edge parent)
		{
			this.parent = parent;
		}

		void setValue(VAL value)
		{
			this.value      = Optional.of(value);
			this.isTerminal = true;
		}

		@Override
		public Optional<VAL> getValue()
		{
			return value;
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
	}

	// =========================================================================

	private class Edge implements IEdge<VAL, LBL>
	{
		LBL  label;
		Node parent;
		Node child;

		Edge(LBL label, Node parent, Node child)
		{
			this.label  = label;
			this.parent = parent;
			this.child  = child;
			TreeBuilder.this.vocab.add(label);
			parent.addEdge(this);
			child.setParent(this);
		}

		@Override
		public LBL getLabel()
		{
			return label;
		}

		@Override
		public INode<VAL, LBL> getParent()
		{
			return parent;
		}

		@Override
		public INode<VAL, LBL> getChild()
		{
			return child;
		}
	}

	// =========================================================================

	private Node root, currentNode;

	private Set<LBL> vocab;

	public TreeBuilder()
	{
		clear();
	}

	// =========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> clear()
	{
		root  = new Node();
		vocab = new HashSet<>();

		currentNode = root;
		return this;
	}

	@Override
	public boolean isEmpty()
	{
		return root.children.isEmpty();
	}

	@Override
	public void setRooted(boolean isRooted)
	{
		root.isRooted = isRooted;
	}

	@Override
	public INode<VAL, LBL> getRoot()
	{
		return root;
	}

	@Override
	public boolean isRooted()
	{
		return root.isRooted;
	}

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		return ((Node) node).children;
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		return Optional.ofNullable(((Node) node).parent);
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return Collections.unmodifiableCollection(vocab);
	}

	@Override
	public ITreeBuilder<VAL, LBL> add(LBL label)
	{
		Node newNode = new Node();
		new Edge(label, currentNode, newNode);
		currentNode = newNode;
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> end()
	{
		if (currentNode == root)
			throw new InvalidParameterException();

		currentNode = (Node) currentNode.parent.getParent();
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> end(VAL value)
	{
		currentNode.setValue(value);
		end();
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> endTerminal()
	{
		currentNode.isTerminal = true;
		end();
		return this;
	}

	// =========================================================================

	@Override
	public String toString()
	{
		return Trees.toString(this);
	}
}
