package insomnia.implem.data.creational;

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
import insomnia.data.creational.AbstractTreeBuilder;
import insomnia.data.creational.ITreeBuilder;

/**
 * An implementation of a tree builder.
 * 
 * @author zuri
 * @param <VAL>
 * @param <LBL>
 */
public final class TreeBuilder<VAL, LBL> extends AbstractTreeBuilder<VAL, LBL>
{
	boolean isPath;

	private Node<VAL, LBL> root, currentNode;

	private int[] coordinates;

	private Set<LBL> vocab;

	public TreeBuilder()
	{
		super();
		reset();
	}

	public TreeBuilder(ITree<VAL, LBL> tree)
	{
		this();
		reset(tree);
	}

	// ==========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> reset(ITree<VAL, LBL> src)
	{
		reset();
		tree(src, true);
		setRooted(src.isRooted());
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> reset()
	{
		isPath      = true;
		coordinates = null;
		root        = new Node<>();
		vocab       = new HashSet<>();

		currentNode = root;
		return this;
	}

	// ==========================================================================

	@Override
	public Node<VAL, LBL> getCurrentNode()
	{
		return currentNode;
	}

	@Override
	public int[] getCurrentCoordinates()
	{
		if (coordinates != null)
			return coordinates;

		coordinates = super.getCurrentCoordinates();
		return coordinates;
	}

	@Override
	public ITreeBuilder<VAL, LBL> setCurrentCoordinates(int... coordinates)
	{
		this.coordinates = coordinates.clone();
		currentNode      = (Node<VAL, LBL>) ITree.followIndex(this, coordinates);
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> setCurrentNode(INode<VAL, LBL> currentNode)
	{
		this.currentNode = (Node<VAL, LBL>) currentNode;
		coordinates      = null;
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> setValue(VAL val)
	{
		currentNode.setValue(val);
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> setLabel(LBL label)
	{
		if (currentNode == root)
			throw new IndexOutOfBoundsException("The root don't have a parent");

		getParent(currentNode).get().setLabel(label);
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> setTerminal(boolean terminal)
	{
		if (currentNode.isTerminal() == terminal)
			return this;
		if (terminal && currentNode.getChildren().size() > 0)
			throw new IllegalStateException("Only a leaf can be terminal");

		currentNode.setTerminal(terminal);
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> removeChilds()
	{
		currentNode.removeAllEdges();

		if (!isPath)
			isPath = ITree.isPath(this);

		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> removeUp()
	{
		var edge = getParent(getCurrentNode());
		goUp();
		currentNode.removeEdge((Edge<VAL, LBL>) edge.get());

		if (!isPath && currentNode.getChildren().size() <= 1)
			isPath = ITree.isPath(this);

		return null;
	}

	// ==========================================================================

	@Override
	public boolean isPath()
	{
		return isPath;
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
	public Node<VAL, LBL> getRoot()
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
		return Collections.unmodifiableList(((Node<VAL, LBL>) node).getChildren());
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
	public ITreeBuilder<VAL, LBL> parent(LBL label, VAL val)
	{
		Optional<IEdge<VAL, LBL>> popt = getParent(getCurrentNode());
		Node<VAL, LBL>            newNode;
		newNode = new Node<>(val);

		if (popt.isPresent())
		{
			Edge<VAL, LBL> parent = (Edge<VAL, LBL>) popt.get();
			parent.setChild(newNode);
			new Edge<>(label, newNode, getCurrentNode(), vocab);
		}
		else
		{
			new Edge<>(label, newNode, getRoot(), vocab);
			// Change the root node
			boolean isRooted = isRooted();
			root.setRooted(false);
			root = newNode;
			root.setRooted(isRooted);
		}
		return this;
	}

	/**
	 * Update the isPath nature of the builder.
	 * Call this before to add a new edge.
	 */
	private void updateIsPath(Node<VAL, LBL> currentNode)
	{
		if (isPath && currentNode.getChildren().size() >= 1)
			isPath = false;
	}

	@Override
	public ITreeBuilder<VAL, LBL> addChild(int pos)
	{
		Node<VAL, LBL> newNode = new Node<>();
		new Edge<>(pos, null, currentNode, newNode, vocab);
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> addChildDown(int pos)
	{
		addChild(pos);
		goDown(pos);
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> addChild(LBL label, VAL val, boolean isTerminal)
	{
		if (currentNode.isTerminal())
			throw new IllegalStateException("A terminal value can't have a child");

		Node<VAL, LBL> newNode = new Node<>(val);
		newNode.setTerminal(isTerminal);
		updateIsPath(currentNode);
		new Edge<>(label, currentNode, newNode, vocab);
		return this;
	}

	private Edge<VAL, LBL> p_addChild(Node<VAL, LBL> currentNode, LBL label, VAL val, boolean isTerminal)
	{
		Node<VAL, LBL> newNode = new Node<>();
		updateIsPath(currentNode);
		return new Edge<>(label, currentNode, newNode, vocab);
	}

	@Override
	public ITreeBuilder<VAL, LBL> tree(ITree<VAL, LBL> tree, INode<VAL, LBL> treeRoot, boolean rewriteRootVal)
	{
		if (currentNode.isTerminal())
			throw new IllegalStateException("A terminal value can't have a child");

		Deque<Pair<Node<VAL, LBL>, INode<VAL, LBL>>> nodes = new LinkedList<>();
		nodes.add(Pair.of(currentNode, treeRoot));

		VAL treeRootCopy = treeRoot.getValue();

		if (!rewriteRootVal)
			treeRoot.setValue(currentNode.getValue());

		Pair<Node<VAL, LBL>, INode<VAL, LBL>> pair;

		while (null != (pair = nodes.poll()))
		{
			INode<VAL, LBL> treeNode = pair.getRight();
			Node<VAL, LBL>  bnode    = pair.getLeft();

			bnode.setTerminal(treeNode.isTerminal());
			bnode.setValue(treeNode.getValue());

			for (IEdge<VAL, LBL> edge : tree.getChildren(treeNode))
			{
				Edge<VAL, LBL> newEdge = p_addChild(bnode, edge.getLabel(), edge.getChild().getValue(), edge.getChild().isTerminal());
				nodes.add(Pair.of(newEdge.getChild(), edge.getChild()));
			}
		}

		if (!rewriteRootVal)
			treeRoot.setValue(treeRootCopy);

		return this;
	}
}
