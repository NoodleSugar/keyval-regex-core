package insomnia.data.creational;

import insomnia.data.AbstractPath;
import insomnia.data.IPath;

public abstract class AbstractPathBuilder<VAL, LBL> extends AbstractPath<VAL, LBL> implements IPathBuilder<VAL, LBL>
{

	// =========================================================================

	@Override
	public IPathBuilder<VAL, LBL> parent(LBL label)
	{
		return parent(label, null);
	}

	@Override
	public IPathBuilder<VAL, LBL> parentUp(IPath<VAL, LBL> path)
	{
		parent(path);
		goUp(path.nbLabels());
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> parentUp(LBL label)
	{
		parent(label);
		goUp();
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> parentUp(LBL label, VAL val)
	{
		parent(label, val);
		goUp();
		return this;
	}

	// =========================================================================

	@Override
	public IPathBuilder<VAL, LBL> child(LBL label)
	{
		return child(label, null);
	}

	@Override
	public IPathBuilder<VAL, LBL> childDown(IPath<VAL, LBL> path)
	{
		child(path);
		goDown(path.nbLabels());
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> childDown(LBL label)
	{
		child(label);
		goDown();
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> childDown(LBL label, VAL val)
	{
		child(label, val);
		goDown();
		return this;
	}

	// =========================================================================

	@Override
	public IPathBuilder<VAL, LBL> goToRoot()
	{
		return setCurrentPos(0);
	}

	@Override
	public IPathBuilder<VAL, LBL> goToLeaf()
	{
		return setCurrentPos(nbNodes() - 1);
	}

	// =========================================================================

	@Override
	public IPathBuilder<VAL, LBL> goUp()
	{
		return goUp(1);
	}

	@Override
	public IPathBuilder<VAL, LBL> goUp(int nb)
	{
		assert (nb > 0);
		int currentPos = getCurrentPos();
		currentPos -= nb;
		setCurrentPos(currentPos);
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> goDown()
	{
		return goDown(1);
	}

	@Override
	public IPathBuilder<VAL, LBL> goDown(int nb)
	{
		assert (nb > 0);
		int currentPos = getCurrentPos();
		currentPos += nb;
		setCurrentPos(currentPos);
		return this;
	}
}
