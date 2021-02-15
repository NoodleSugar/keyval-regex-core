package insomnia.implem.data.creational;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;
import insomnia.data.creational.ISubTreeBuilder;
import insomnia.implem.data.Trees;

/**
 * An implementation of a sub-tree builder which select existing nodes and edges from a tree.
 * 
 * @author zuri
 */
public final class SubTreeBuilder<VAL, LBL> implements ISubTreeBuilder<VAL, LBL>
{
	private ITree<VAL, LBL> parentTree;

	private INode<VAL, LBL> root;

	private Map<INode<VAL, LBL>, List<IEdge<VAL, LBL>>> childrenOf;
	private Map<INode<VAL, LBL>, IEdge<VAL, LBL>>       parentOf;

	private Set<LBL>             vocab;
	private Set<IEdge<VAL, LBL>> edges;

	private SubTreeBuilder()
	{
		childrenOf = new HashMap<>();
		parentOf   = new HashMap<>();
		vocab      = new HashSet<>();
		edges      = new HashSet<>();
	}

	public SubTreeBuilder(ITree<VAL, LBL> parentTree)
	{
		this();
		setParentTree(parentTree);
	}

	private void setParentTree(ITree<VAL, LBL> parentTree)
	{
		this.parentTree = parentTree;
		setRoot(parentTree.getRoot());
	}

	// =========================================================================

	@Override
	public INode<VAL, LBL> getRoot()
	{
		return root;
	}

	@Override
	public boolean isEmpty()
	{
		return childrenOf.getOrDefault(root, Collections.emptyList()).isEmpty();
	}

	@Override
	public boolean isRooted()
	{
		return root.isRooted();
	}

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		return childrenOf.getOrDefault(node, Collections.emptyList());
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		return Optional.ofNullable(parentOf.get(node));
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return Collections.unmodifiableCollection(vocab);
	}

	// =========================================================================

	@Override
	public SubTreeBuilder<VAL, LBL> setRoot(INode<VAL, LBL> root)
	{
		this.root = root;
		return this;
	}

	@Override
	public SubTreeBuilder<VAL, LBL> reset()
	{
		parentOf.clear();
		childrenOf.clear();
		vocab.clear();
		root = parentTree.getRoot();
		return this;
	}

	@Override
	public SubTreeBuilder<VAL, LBL> reset(ITree<VAL, LBL> parentTree)
	{
		reset();
		setParentTree(parentTree);
		return this;
	}

	private void addEdge(IEdge<VAL, LBL> child)
	{
		if (edges.contains(child))
			return;

		edges.add(child);

		List<IEdge<VAL, LBL>> childsList = childrenOf.get(child.getParent());

		if (null == childsList)
		{
			childsList = new ArrayList<>(parentTree.getChildren(child.getParent()).size());
			childrenOf.put(child.getParent(), childsList);
		}
		parentOf.put(child.getChild(), child);
		childsList.add(child);
	}

	@Override
	public SubTreeBuilder<VAL, LBL> add(IEdge<VAL, LBL> edge)
	{
		addEdge(edge);
		return this;
	}

	@Override
	public SubTreeBuilder<VAL, LBL> addTree(INode<VAL, LBL> root)
	{
		return addTree(parentTree, root);
	}

	@Override
	public SubTreeBuilder<VAL, LBL> addTree(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		List<IEdge<VAL, LBL>> currentEdges = new ArrayList<>();
		List<IEdge<VAL, LBL>> nextEdge     = new ArrayList<>();

		currentEdges.addAll(tree.getChildren(root));

		while (!currentEdges.isEmpty())
		{
			for (IEdge<VAL, LBL> edge : currentEdges)
			{
				addEdge(edge);
				nextEdge.addAll(tree.getChildren(edge.getChild()));
			}
			List<IEdge<VAL, LBL>> tmp = currentEdges;
			currentEdges = nextEdge;
			nextEdge     = tmp;
			nextEdge.clear();
		}
		return this;
	}

	// =========================================================================

	@Override
	public String toString()
	{
		return Trees.toString(this);
	}
}
