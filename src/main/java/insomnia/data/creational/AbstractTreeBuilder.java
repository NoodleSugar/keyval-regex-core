package insomnia.data.creational;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

import insomnia.AbstractTree;
import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.ITree;

public abstract class AbstractTreeBuilder<VAL, LBL> extends AbstractTree<VAL, LBL> implements ITreeBuilder<VAL, LBL>
{
	@Override
	public ITreeBuilder<VAL, LBL> setComplete()
	{
		return setRooted().setLeavesTerminal();
	}

	@Override
	public ITreeBuilder<VAL, LBL> setRooted()
	{
		return setRooted(true);
	}

	@Override
	public ITreeBuilder<VAL, LBL> setLeavesTerminal()
	{
		return setLeavesTerminal(true);
	}

	@Override
	public ITreeBuilder<VAL, LBL> setLeavesTerminal(boolean terminal)
	{
		var currentNode = getCurrentNode();

		for (var node : ITree.getLeaves(this))
		{
			setCurrentNode(node);
			setTerminal(terminal);
		}
		setCurrentNode(currentNode);
		return this;
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

	@Override
	public ITreeBuilder<VAL, LBL> removeAt(int... coordinates)
	{
		var node = getCurrentNode();
		goDown(coordinates);
		removeUp();
		setCurrentNode(node);
		return this;
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
	public ITreeBuilder<VAL, LBL> addChildAt(int... coordinates)
	{
		addChildAtDown(coordinates);
		goUp(coordinates.length);
		return this;
	}

	public ITreeBuilder<VAL, LBL> addChildAtDown(int... coordinates)
	{
		goDown(Arrays.copyOfRange(coordinates, 0, coordinates.length - 1));
		addChildDown(coordinates[coordinates.length - 1]);
		return this;
	}

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
	public ITreeBuilder<VAL, LBL> tree(ITree<VAL, LBL> tree, boolean rewriteRootVal)
	{
		return tree(tree, tree.getRoot(), rewriteRootVal);
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
		var node = ITree.followIndex(this, getCurrentNode(), coordinates);

		if (null == node)
			throw new IndexOutOfBoundsException("Invalid coordinates " + ArrayUtils.toString(coordinates));

		setCurrentNode(node);
		return this;
	}
}
