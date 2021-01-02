package insomnia.implem.kv.data;

import java.util.Collections;
import java.util.List;

import insomnia.data.AbstractPath;
import insomnia.data.INode;
import insomnia.data.IPath;

public class KVPath extends AbstractPath<KVValue, KVLabel>
{
	KVPath()
	{
		super(Collections.emptyList());
	}

	// Constructeur de sous-chemin
	KVPath(IPath<KVValue, KVLabel> path, int begin, int end)
	{
		super(path, begin, end);
	}

	KVPath(List<KVLabel> labels)
	{
		super(labels);
	}

	KVPath(List<KVLabel> labels, KVValue value)
	{
		super(labels, value);
	}

	// path doit contenir au moins une clé
	KVPath(boolean isRooted, List<KVLabel> labels)
	{
		super(isRooted, labels);
	}

	KVPath(boolean isRooted, List<KVLabel> labels, KVValue value)
	{
		super(isRooted, labels, value);
	}

	KVPath(boolean isRooted, boolean isTerminal, List<KVLabel> labels)
	{
		super(isRooted, isTerminal, labels);
	}

	// path doit contenir au moins une clé
	KVPath(boolean isRooted, boolean isTerminal, List<KVLabel> labels, KVValue value)
	{
		super(isRooted, isTerminal, labels, value);
	}

	@Override
	public KVPath subPath(int begin, int end)
	{
		return new KVPath(this, begin, end);
	}

	// =========================================================================

	@Override
	public List<KVEdge> getChildren(INode<KVValue, KVLabel> node)
	{
		assert (node instanceof KVPathNode);
		KVPathNode kvnode = (KVPathNode) node;

		if (kvnode.pos == nbLabels())
			return Collections.emptyList();

		int     pos   = kvnode.pos;
		KVValue value = (pos == nbLabels() - 1) ? getValue().orElse(null) : null;
		return Collections.singletonList(new KVEdge(kvnode, new KVPathNode(pos + 1, value), getLabels().get(pos)));
	}

	@Override
	public KVEdge getParent(INode<KVValue, KVLabel> node)
	{
		assert (node instanceof KVPathNode);
		KVPathNode kvnode = (KVPathNode) node;

		if (kvnode.pos == 0)
			return null;

		int pos = kvnode.pos - 1;
		return new KVEdge(new KVPathNode(pos, null), kvnode, getLabels().get(pos));
	}

	@Override
	public INode<KVValue, KVLabel> getRoot()
	{
		return new KVPathNode(0, null);
	}

	@Override
	public IPath<KVValue, KVLabel> setValue(KVValue value)
	{
		return new KVPath(isRooted(), getLabels(), value);
	}
}
