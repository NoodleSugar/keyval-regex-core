package insomnia.implem.data.creational;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.creational.ITreeBuilder;
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
	private Node<VAL, LBL> root, currentNode;

	private Set<LBL> vocab;

	public TreeBuilder()
	{
		clear();
	}

	// =========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> clear()
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
	public ITreeBuilder<VAL, LBL> add(LBL label)
	{
		Node<VAL, LBL> newNode = new Node<>();
		new Edge<>(label, currentNode, newNode, vocab);
		currentNode = newNode;
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
		return Trees.toString(this);
	}
}
