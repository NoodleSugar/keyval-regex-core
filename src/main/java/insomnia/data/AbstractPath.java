package insomnia.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

import insomnia.AbstractTree;
import insomnia.implem.data.Paths;

public abstract class AbstractPath<VAL, LBL> extends AbstractTree<VAL, LBL> implements IPath<VAL, LBL>
{

	@Override
	public IPath<VAL, LBL> subPath(int from, int to)
	{
		return Paths.subPath(this, from, to);
	}

	// =========================================================================o

	@Override
	public INode<VAL, LBL> getRoot()
	{
		return getNodes().get(0);
	}

	@Override
	public INode<VAL, LBL> getLeaf()
	{
		return getNodes().get(nbNodes() - 1);
	}

	@Override
	public List<INode<VAL, LBL>> getLeaves()
	{
		return Collections.singletonList(getLeaf());
	}

	@Override
	public List<INode<VAL, LBL>> getLeaves(INode<VAL, LBL> node)
	{
		return getLeaves();
	}

	@Override
	public List<INode<VAL, LBL>> getNodes()
	{
		return getNodes(getRoot());
	}

	@Override
	public IEdge<VAL, LBL> getChild(INode<VAL, LBL> node)
	{
		List<? extends IEdge<VAL, LBL>> childs = getChildren(node);

		if (childs.isEmpty())
			return null;

		return childs.get(0);
	}

	@Override
	public int nbLabels()
	{
		return getLabels().size();
	}

	@Override
	public int nbNodes()
	{
		return getNodes().size();
	}

	@Override
	public boolean isEmpty()
	{
		return getLabels().isEmpty() && INode.isEmpty(getRoot());
	}

	@Override
	public List<VAL> getValues()
	{
		return INode.getValues(getNodes());
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return new HashSet<>(getLabels());
	}

	@Override
	public int size()
	{
		return getLabels().size() + BooleanUtils.toInteger(isRooted()) + BooleanUtils.toInteger(isTerminal());
	}

	@Override
	public boolean isPath()
	{
		return true;
	}

	@Override
	public boolean isTerminal()
	{
		return getLeaf().isTerminal();
	}

	@Override
	public boolean isComplete()
	{
		return isRooted() && isTerminal();
	}

	@Override
	public boolean isFree()
	{
		return !isRooted() && !isTerminal();
	}

	@Override
	public boolean isFixed()
	{
		return isRooted() || isTerminal();
	}

	// =========================================================================
	// Object Override

	@Override
	public String toString()
	{
		return IPath.toString(this);
	}
}
