package insomnia.implem.kv.data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import insomnia.data.AbstractPath;
import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.implem.kv.data.KVNodes.KVNode;

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
		assert (node instanceof KVNode);
		KVNode kvnode = (KVNode) node;

		if (kvnode.getPos() == nbLabels())
			return Collections.emptyList();

		int     pos        = kvnode.getPos();
		boolean lastPos    = pos == nbLabels() - 1;
		boolean isTerminal = lastPos && isTerminal();

		Optional<KVValue> value = lastPos ? getValue() : Optional.empty();
		return Collections.singletonList(new KVEdge(kvnode, KVNodes.create(pos + 1, false, isTerminal, value), getLabels().get(pos)));
	}

	@Override
	public Optional<IEdge<KVValue, KVLabel>> getParent(INode<KVValue, KVLabel> node)
	{
		assert (node instanceof KVNode);
		KVNode kvnode = (KVNode) node;

		if (kvnode.getPos() == 0)
			return Optional.empty();

		int     pos      = kvnode.getPos() - 1;
		boolean isRooted = pos == 0 && isRooted();
		return Optional.of(new KVEdge(KVNodes.create(pos, isRooted, false, Optional.empty()), kvnode, getLabels().get(pos)));
	}

	@Override
	public INode<KVValue, KVLabel> getRoot()
	{
		boolean isTerminal = isEmpty() && isTerminal();
		return KVNodes.create(0, isRooted(), isTerminal, Optional.empty());
	}
}
