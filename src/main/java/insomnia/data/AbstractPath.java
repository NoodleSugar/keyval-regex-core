package insomnia.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.BooleanUtils;

public abstract class AbstractPath<VAL, LBL> implements IPath<VAL, LBL>
{

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
	public List<INode<VAL, LBL>> getNodes()
	{
		return getNodes(getRoot());
	}

	@Override
	public List<INode<VAL, LBL>> getNodes(INode<VAL, LBL> node)
	{
		return ITree.getNodes(this, node);
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges()
	{
		return getEdges(getRoot());
	}

	@Override
	public List<IEdge<VAL, LBL>> getEdges(INode<VAL, LBL> node)
	{
		return ITree.getEdges(this, node);
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
		return getLabels().isEmpty();
	}

	@Override
	public List<VAL> getValues()
	{
		return IterableUtils.toList(IterableUtils.transformedIterable(getNodes(), INode::getValue));
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
	public boolean isRooted()
	{
		return getRoot().isRooted();
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
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof IPath))
			return false;

		IPath<?, ?> path = (IPath<?, ?>) o;

		if (nbLabels() != path.nbLabels())
			return false;
		if (!IPath.areEquals(this, path))
			return false;

		Iterator<?> a = IteratorUtils.transformedIterator(getNodes().iterator(), INode::getValue);
		Iterator<?> b = IteratorUtils.transformedIterator(path.getNodes().iterator(), INode::getValue);

		while (a.hasNext() && a.next() == b.next())
			;
		return !a.hasNext();
	}

	@Override
	public int hashCode()
	{
		return getLabels().hashCode() + getValues().hashCode() + BooleanUtils.toInteger(isRooted()) + BooleanUtils.toInteger(isTerminal());
	}

	@Override
	public String toString()
	{
		return IPath.toString(this);
	}
}
