package insomnia.implem.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

final class Tree<VAL, LBL> implements ITree<VAL, LBL>
{
	private INode<VAL, LBL> root;

	private Map<INode<VAL, LBL>, List<IEdge<VAL, LBL>>> childrenOf;
	private Map<INode<VAL, LBL>, IEdge<VAL, LBL>>       parentOf;

	private Collection<LBL> vocabulary;

	private Tree()
	{
		childrenOf = new HashMap<>();
		parentOf   = new HashMap<>();
		vocabulary = new HashSet<>();
	}

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

	private IEdge<VAL, LBL> createEdge(INode<VAL, LBL> parent, INode<VAL, LBL> child, LBL label, List<IEdge<VAL, LBL>> childrenOf)
	{
		IEdge<VAL, LBL> newEdge = Edges.create(parent, child, label);
		parentOf.put(child, newEdge);
		childrenOf.add(newEdge);
		return newEdge;
	}

	private void recursiveConstruct(INode<VAL, LBL> tNode, ITree<VAL, LBL> src, INode<VAL, LBL> srcNode)
	{
		List<? extends IEdge<VAL, LBL>> srcChildren = src.getChildren(srcNode);
		List<IEdge<VAL, LBL>>           childrenOf  = new ArrayList<>(srcChildren.size());

		for (IEdge<VAL, LBL> srcEdge : srcChildren)
		{
			vocabulary.add(srcEdge.getLabel());
			INode<VAL, LBL> srcChild = srcEdge.getChild();
			INode<VAL, LBL> tChild   = Nodes.create(srcChild);

			createEdge(tNode, tChild, srcEdge.getLabel(), childrenOf);
			recursiveConstruct(tChild, src, srcChild);
		}
		this.childrenOf.put(tNode, Collections.unmodifiableList(childrenOf));
	}

	// =========================================================================

	public <RVAL, RLBL> Tree<RVAL, RLBL> map(Function<VAL, RVAL> mapVal, Function<LBL, RLBL> mapLabel)
	{
		return map(this, mapVal, mapLabel);
	}

	private <SVAL, SLBL> INode<VAL, LBL> mapNode(INode<SVAL, SLBL> srcNode, Function<SVAL, VAL> mapVal)
	{
		Optional<SVAL> srcNodeValue = srcNode.getValue();

		if (srcNodeValue.isPresent())
			return Nodes.create(srcNode, Optional.of(mapVal.apply(srcNodeValue.get())));

		return Nodes.create(srcNode, Optional.empty());
	}

	private <SLBL> LBL mapLabel(SLBL srcLabel, Function<SLBL, LBL> mapLabel)
	{
		return mapLabel.apply(srcLabel);
	}

	static <RVAL, RLBL, SVAL, SLBL> Tree<RVAL, RLBL> map(ITree<SVAL, SLBL> src, Function<SVAL, RVAL> fmapVal, Function<SLBL, RLBL> fmapLabel)
	{
		Tree<RVAL, RLBL> ret = new Tree<>();
		ret.root = ret.mapNode(src.getRoot(), fmapVal);
		ret.recursiveMap(ret.root, src, src.getRoot(), fmapVal, fmapLabel);
		return ret;
	}

	private <SVAL, SLBL> void recursiveMap( //
		INode<VAL, LBL> tNode, ITree<SVAL, SLBL> src, INode<SVAL, SLBL> srcNode, //
		Function<SVAL, VAL> fmapVal, Function<SLBL, LBL> fmapLabel //
	)
	{
		List<? extends IEdge<SVAL, SLBL>> srcChildren = src.getChildren(srcNode);
		List<IEdge<VAL, LBL>>             childrenOf  = new ArrayList<>(srcChildren.size());

		for (IEdge<SVAL, SLBL> srcEdge : srcChildren)
		{
			INode<VAL, LBL> childNode = mapNode(srcEdge.getChild(), fmapVal);
			createEdge(tNode, childNode, mapLabel(srcEdge.getLabel(), fmapLabel), childrenOf);
			recursiveMap(childNode, src, srcEdge.getChild(), fmapVal, fmapLabel);
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
	public boolean isEmpty()
	{
		return childrenOf.isEmpty();
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
		return vocabulary;
	}

	@Override
	public String toString()
	{
		return Trees.toString(this);
	}
}
