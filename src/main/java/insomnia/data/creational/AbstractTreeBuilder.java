package insomnia.data.creational;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

public abstract class AbstractTreeBuilder<VAL, LBL> implements ITreeBuilder<VAL, LBL>
{
	@Override
	public List<INode<VAL, LBL>> getNodes()
	{
		return ITree.getNodes(this);
	}

	@Override
	public List<INode<VAL, LBL>> getNodes(INode<VAL, LBL> node)
	{
		return ITree.getNodes(this, node);
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges()
	{
		return ITree.getEdges(this);
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges(INode<VAL, LBL> node)
	{
		return ITree.getEdges(this, node);
	}

	// ==========================================================================

	@Override
	public int[] getCurrentCoordinates()
	{
		List<Integer>   coordinates = new LinkedList<>();
		INode<VAL, LBL> node        = getCurrentNode();

		while (true)
		{
			Optional<IEdge<VAL, LBL>> opt = getParent(node);

			if (!opt.isPresent())
				break;

			IEdge<VAL, LBL> parent = opt.get();
			node = parent.getParent();
			coordinates.add(0, getChildren(node).indexOf(parent));
		}
		return coordinates.stream().mapToInt(Integer::valueOf).toArray();
	}

	// ==========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> parent(LBL label)
	{
		return parent(label, null);
	}

	@Override
	public ITreeBuilder<VAL, LBL> parentUp(LBL label)
	{
		parent(label);
		goUp();
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> parentUp(LBL label, VAL val)
	{
		parent(label, val);
		goUp();
		return this;
	}

	// ==========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> addChild(LBL label)
	{
		return addChild(label, null);
	}

	@Override
	public ITreeBuilder<VAL, LBL> addChild(LBL label, VAL val)
	{
		return addChild(label, val, false);
	}

	@Override
	public ITreeBuilder<VAL, LBL> addChildDown(LBL label)
	{
		return addChildDown(label, null, false);
	}

	@Override
	public ITreeBuilder<VAL, LBL> addChildDown(LBL label, VAL val)
	{
		return addChildDown(label, val, false);
	}

	@Override
	public ITreeBuilder<VAL, LBL> addChildDown(LBL label, VAL val, boolean isTerminal)
	{
		int i = getChildren(getCurrentNode()).size();
		addChild(label, val, isTerminal);
		goDown(i);
		return this;
	}

	// ==========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> tree(ITree<VAL, LBL> tree)
	{
		return tree(tree, tree.getRoot());
	}

	// ==========================================================================

	@Override
	public ITreeBuilder<VAL, LBL> goToRoot()
	{
		setCurrentCoordinates();
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> goUp()
	{
		return goUp(1);
	}

	@Override
	public ITreeBuilder<VAL, LBL> goUp(int nb)
	{
		int coordinates[] = getCurrentCoordinates();

		if (nb > coordinates.length)
			throw new IndexOutOfBoundsException(String.format("Nb parents: %d, ask: %d", coordinates.length, nb));

		setCurrentCoordinates(ArrayUtils.subarray(coordinates, 0, coordinates.length - nb));
		return this;
	}

	@Override
	public ITreeBuilder<VAL, LBL> goDown(int... coordinates)
	{
		setCurrentNode(ITree.followIndex(this, getCurrentNode(), coordinates));
		return this;
	}

	// ==========================================================================

	@Override
	public String toString()
	{
		return ITree.treeOrPathToString(this);
	}
}
