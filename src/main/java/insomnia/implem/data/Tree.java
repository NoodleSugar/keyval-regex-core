package insomnia.implem.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import insomnia.AbstractTree;
import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

final class Tree<VAL, LBL> extends AbstractTree<VAL, LBL>
{
	private INode<VAL, LBL> root;

	private Map<INode<VAL, LBL>, List<IEdge<VAL, LBL>>> childrenOf;
	private Map<INode<VAL, LBL>, IEdge<VAL, LBL>>       parentOf;

	private Collection<LBL> vocabulary;

	private Tree()
	{
	}

	private static <VAL, LBL> Tree<VAL, LBL> mutable()
	{
		Tree<VAL, LBL> ret = new Tree<>();
		ret.childrenOf = new HashMap<>();
		ret.parentOf   = new HashMap<>();
		ret.vocabulary = new HashSet<>();
		return ret;
	}

	// =========================================================================

	private IEdge<VAL, LBL> createEdge(INode<VAL, LBL> parent, INode<VAL, LBL> child, LBL label)
	{
		IEdge<VAL, LBL> newEdge = Edges.create(parent, child, label);
		parentOf.put(child, newEdge);
		childrenOf.get(parent).add(newEdge);
		return newEdge;
	}

	// =========================================================================

	static <VAL, LBL> Tree<VAL, LBL> copy(ITree<VAL, LBL> tree)
	{
		return copy(tree, tree.getRoot());
	}

	static <VAL, LBL> Tree<VAL, LBL> copy(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		return map(tree, root, Nodes::create, Function.identity());
	}

	static <VAL, LBL> Tree<VAL, LBL> subTree(ITree<VAL, LBL> tree)
	{
		return subTree(tree, tree.getRoot());
	}

	static <VAL, LBL> Tree<VAL, LBL> subTree(ITree<VAL, LBL> tree, INode<VAL, LBL> root)
	{
		return map(tree, root, Function.identity(), Function.identity());
	}

	// =========================================================================

	static <VAL, LBL, TOVAL, TOLBL> Tree<TOVAL, TOLBL> map( //
		ITree<VAL, LBL> tree, //
		INode<VAL, LBL> root, //
		Function<INode<VAL, LBL>, INode<TOVAL, TOLBL>> fmapNode, //
		Function<LBL, TOLBL> fmapLabel //
	)
	{
		Tree<TOVAL, TOLBL> ret   = mutable();
		Queue<INode<?, ?>> nodes = new LinkedList<>();
		ret.root = fmapNode.apply(root);

		if (tree.getChildren(root).isEmpty())
			return ret;

		nodes.add(root);
		nodes.add(ret.root);

		while (!nodes.isEmpty())
		{
			@SuppressWarnings("unchecked")
			INode<VAL, LBL>     node    = (INode<VAL, LBL>) nodes.poll();
			@SuppressWarnings("unchecked")
			INode<TOVAL, TOLBL> newNode = (INode<TOVAL, TOLBL>) nodes.poll();

			var nodeChilds = tree.getChildren(node);
			ret.childrenOf.put(newNode, new ArrayList<>(nodeChilds.size()));

			for (var edge : nodeChilds)
			{
				var nodeChild    = edge.getChild();
				var edgeChilds   = tree.getChildren(nodeChild);
				var newNodeChild = fmapNode.apply(nodeChild);
				ret.createEdge(newNode, newNodeChild, fmapLabel.apply(edge.getLabel()));

				if (edgeChilds.isEmpty())
					continue;

				nodes.add(nodeChild);
				nodes.add(newNodeChild);
			}
		}
		return ret;
	}

	// =========================================================================

	@Override
	public INode<VAL, LBL> getRoot()
	{
		return root;
	}

	/**
	 * This Tree class must never be used to be a Path.
	 * 
	 * @return always false
	 */
	@Override
	public boolean isPath()
	{
		assert (!ITree.isPath(this));
		return false;
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges()
	{
		return childrenOf.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
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
		return vocabulary;
	}
}
