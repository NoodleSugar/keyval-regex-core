package insomnia.implem.data.creational;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.creational.ITreeBuilder;

/**
 * An implementation of a tree builder.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public final class TreeBuilder<VAL, LBL> implements ITreeBuilder<VAL, LBL>
{
	private Node<VAL, LBL> root, currentNode;

	private Set<LBL> vocab;

	public TreeBuilder()
	{
		reset();
	}

	public TreeBuilder(ITree<VAL, LBL> tree)
	{
		reset();
		tree(tree, tree.getRoot());
	}

	// =========================================================================

	// =========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> reset()
	{
		root  = new Node<>();
		vocab = new HashSet<>();

		currentNode = root;
		return this;
	}

	@Override
	public boolean isEmpty()
	{
		return root.getChildren().isEmpty();
	}

	@Override
	public ITreeBuilder<VAL, LBL> setRooted(boolean isRooted)
	{
		root.setRooted(isRooted);
		return this;
	}

	@Override
	public INode<VAL, LBL> getRoot()
	{
		return root;
	}

	@Override
	public boolean isRooted()
	{
		return root.isRooted();
	}

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		return ((Node<VAL, LBL>) node).getChildren();
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		return Optional.ofNullable(((Node<VAL, LBL>) node).getParent());
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return Collections.unmodifiableCollection(vocab);
	}

	@Override
	public ITreeBuilder<VAL, LBL> setCurrentNode(INode<VAL, LBL> currentNode)
	{
		this.currentNode = (Node<VAL, LBL>) currentNode;
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> parent(LBL label)
	{
		return parent(label, null);
	}

	@Override
	public ITreeBuilder<VAL, LBL> parent(LBL label, Collection<? super IEdge<VAL, LBL>> addedEdge)
	{
		p_parent(label, addedEdge);
		return this;
	}

	public void p_parent(LBL label, Collection<? super IEdge<VAL, LBL>> addedEdge)
	{
		Node<VAL, LBL>  newNode = new Node<>();
		IEdge<VAL, LBL> newEdge = new Edge<>(label, newNode, root, vocab);

		if (null != addedEdge)
			addedEdge.add(newEdge);

		// Change the root node
		boolean isRooted = isRooted();
		setRooted(false);
		root = newNode;
		root.setRooted(isRooted);
	}

	@Override
	public ITreeBuilder<VAL, LBL> child(LBL label)
	{
		return child(label, null);
	}

	@Override
	public ITreeBuilder<VAL, LBL> child(LBL label, Collection<? super IEdge<VAL, LBL>> addedEdge)
	{
		currentNode = child(currentNode, label, addedEdge);
		return this;
	}

	private Node<VAL, LBL> child(Node<VAL, LBL> currentNode, LBL label, Collection<? super IEdge<VAL, LBL>> addedEdge)
	{
		Node<VAL, LBL>  newNode = new Node<>();
		IEdge<VAL, LBL> newEdge = new Edge<>(label, currentNode, newNode, vocab);

		if (null != addedEdge)
			addedEdge.add(newEdge);

		return newNode;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ITreeBuilder<VAL, LBL> tree(ITree<VAL, LBL> tree, INode<VAL, LBL> treeRoot)
	{
		Deque<Pair<Node<VAL, LBL>, INode<VAL, LBL>>> nodes = new LinkedList<>();
		nodes.add(Pair.of(currentNode, treeRoot));

		Pair<Node<VAL, LBL>, INode<VAL, LBL>> pair;
		List<IEdge<VAL, LBL>>                 addedEdges = new ArrayList<>(1);

		while (null != (pair = nodes.poll()))
		{
			INode<VAL, LBL> treeNode = pair.getRight();
			Node<VAL, LBL>  bnode    = pair.getLeft();

			bnode.setTerminal(treeNode.isTerminal());
			bnode.setValue(treeNode.getValue());

			for (IEdge<VAL, LBL> edge : tree.getChildren(treeRoot))
			{
				child(bnode, edge.getLabel(), addedEdges);
				nodes.add(Pair.of((Node<VAL, LBL>) addedEdges.get(0), treeNode));
				nodes.clear();
			}
		}
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> end()
	{
		if (currentNode == root)
			throw new InvalidParameterException();

		currentNode = (Node<VAL, LBL>) currentNode.getParent().getParent();
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> endTerminal(VAL value)
	{
		currentNode.setValue(value);
		end();
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> endTerminal()
	{
		currentNode.setTerminal(true);
		end();
		return this;
	}

	// =========================================================================

	@Override
	public String toString()
	{
		return ITree.toString(this);
	}
}
