package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Tree<VAL, LBL> implements ITree<VAL, LBL>
{
	private INode<VAL, LBL> root;

	private Map<INode<VAL, LBL>, List<IEdge<VAL, LBL>>> childrenOf;
	private Map<INode<VAL, LBL>, IEdge<VAL, LBL>>       parentOf;

	private Collection<LBL> vocabulary;

	Tree(ITree<VAL, LBL> src)
	{
		childrenOf = new HashMap<>();
		parentOf   = new HashMap<>();

		INode<VAL, LBL> srcNode = src.getRoot();
		root = Nodes.create(srcNode.isRooted(), srcNode.isTerminal(), srcNode.getValue());

		vocabulary = new HashSet<>();
		recursiveConstruct(root, src, srcNode);
		vocabulary = Collections.unmodifiableList(new ArrayList<>(vocabulary));
	}

	private void recursiveConstruct(INode<VAL, LBL> tNode, ITree<VAL, LBL> src, INode<VAL, LBL> srcNode)
	{
		List<? extends IEdge<VAL, LBL>> srcChildren = src.getChildren(srcNode);
		List<IEdge<VAL, LBL>>           childrenOf  = new ArrayList<>(srcChildren.size());

		for (IEdge<VAL, LBL> srcEdge : srcChildren)
		{
			vocabulary.add(srcEdge.getLabel());
			INode<VAL, LBL> srcChild = srcEdge.getChild();
			INode<VAL, LBL> tChild   = Nodes.create(srcChild.isRooted(), srcChild.isTerminal(), srcChild.getValue());

			IEdge<VAL, LBL> tEdge = Edges.create(tNode, tChild, srcEdge.getLabel());
			parentOf.put(tChild, tEdge);
			childrenOf.add(tEdge);
			recursiveConstruct(tChild, src, srcChild);
		}
		this.childrenOf.put(tNode, Collections.unmodifiableList(childrenOf));
	}

	// =========================================================================

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
	public List<? extends IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
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

	@Override
	public String toString()
	{
		return Trees.toString(this);
	}
}
