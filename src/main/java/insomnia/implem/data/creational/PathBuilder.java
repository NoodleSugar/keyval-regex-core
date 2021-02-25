package insomnia.implem.data.creational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.data.creational.AbstractPathBuilder;
import insomnia.data.creational.IPathBuilder;
import insomnia.implem.data.Edges;
import insomnia.implem.data.Nodes;
import insomnia.lib.help.HelpLists;

public final class PathBuilder<VAL, LBL> extends AbstractPathBuilder<VAL, LBL>
{
	private List<LBL>             labels;
	private List<INode<VAL, LBL>> nodes;
	private int                   currentPos;

	public PathBuilder()
	{
		super();
		currentPos = 0;
		labels     = new ArrayList<>();
		nodes      = new ArrayList<>();
		nodes.add(Nodes.create(false, false, null));
	}

	public PathBuilder(IPathBuilder<VAL, LBL> src)
	{
		this();
		reset(src);
	}

	@Override
	public IPath<VAL, LBL> subPath(int from, int to)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IPathBuilder<VAL, LBL> reset()
	{
		labels.clear();
		nodes.clear();
		currentPos = 0;
		nodes.add(Nodes.create());
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> reset(IPathBuilder<VAL, LBL> src)
	{
		labels.clear();
		labels.addAll(src.getLabels());
		nodes.clear();
		nodes.addAll(ListUtils.transformedList(src.getNodes(), n -> Nodes.create(n)));
		currentPos = src.getCurrentPos();
		return this;
	}

	// =========================================================================

	@Override
	public List<LBL> getLabels()
	{
		return HelpLists.staticList(labels);
	}

	@Override
	public List<INode<VAL, LBL>> getNodes()
	{
		return HelpLists.staticList(nodes);
	}

	@Override
	public List<IEdge<VAL, LBL>> getChildren(INode<VAL, LBL> node)
	{
		int i = nodes.indexOf(node);

		if (i == -1 || i == nodes.size() - 1)
			return Collections.emptyList();

		return Collections.singletonList(Edges.create(node, nodes.get(i + 1), labels.get(i)));
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getParent(INode<VAL, LBL> node)
	{
		int i = nodes.indexOf(node);

		if (i == -1 || i == 0)
			return Optional.empty();

		return Optional.of(Edges.create(nodes.get(i - 1), node, labels.get(i - 1)));
	}

	@Override
	public INode<VAL, LBL> getCurrentNode()
	{
		return nodes.get(currentPos);
	}

	// =========================================================================

	@Override
	public IPathBuilder<VAL, LBL> setRooted(boolean rooted)
	{
		INode<VAL, LBL> root = getRoot();
		if (root.isRooted() == rooted)
			return this;
		INode<VAL, LBL> node = Nodes.create(rooted, root.isTerminal(), root.getValue());
		nodes.set(0, node);
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> setTerminal(boolean terminal)
	{
		INode<VAL, LBL> leaf = getLeaf();
		if (leaf.isTerminal() == terminal)
			return this;
		INode<VAL, LBL> node = Nodes.create(leaf.isRooted(), terminal, leaf.getValue());
		nodes.set(nbLabels(), node);
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> parent(IPath<VAL, LBL> path)
	{
		labels.addAll(currentPos, path.getLabels());
		nodes.addAll(currentPos, CollectionUtils.collect(path.getNodes(), n -> Nodes.create(n.getValue())));
		currentPos += path.nbLabels();
		nodes.remove(currentPos); // delete the leaf from path
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> child(IPath<VAL, LBL> path)
	{
		labels.addAll(currentPos, path.getLabels());
		nodes.addAll(currentPos + 1, CollectionUtils.collect(path.getNodes(), n -> Nodes.create(n.getValue())));
		nodes.remove(currentPos + 1); // delete the root from path
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> child(LBL label, VAL val)
	{
		labels.add(currentPos, label);
		nodes.add(currentPos + 1, Nodes.create(false, false, val));
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> parent(LBL label, VAL val)
	{
		labels.add(currentPos, label);
		nodes.add(currentPos, Nodes.create(false, false, val));
		currentPos++;
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> setValue(VAL val)
	{
		INode<VAL, LBL> last = nodes.get(currentPos);
		nodes.set(currentPos, Nodes.create(last, val));
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> dropUp()
	{
		return dropUp(1);
	}

	@Override
	public IPathBuilder<VAL, LBL> dropUp(int nbNodes)
	{
		assert (nbNodes >= 0);
//		if (nodes.size() <= nbNodes || currentPos - nbNodes < 0)
//			throw new IndexOutOfBoundsException();

		int from = currentPos - nbNodes + 1;
		int to   = currentPos + 1;
		nodes.subList(from, to).clear();
		labels.subList(from - 1, to - 1).clear();
		currentPos -= nbNodes;
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> dropDown()
	{
		return dropDown(1);
	}

	@Override
	public IPathBuilder<VAL, LBL> dropDown(int nbNodes)
	{
		assert (nbNodes >= 0);
//		if (nodes.size() <= nbNodes || currentPos + nbNodes >= nodes.size())
//			throw new IndexOutOfBoundsException();

		nodes.subList(currentPos, currentPos + nbNodes).clear();
		labels.subList(currentPos, currentPos + nbNodes).clear();
		return this;
	}

	@Override
	public IPathBuilder<VAL, LBL> setCurrentPos(int pos)
	{
		if (pos < 0 || pos >= nbNodes())
			throw new IndexOutOfBoundsException(String.format("pos=%d size=%d", pos, nbNodes()));

		currentPos = pos;
		return this;
	}

	@Override
	public int getCurrentPos()
	{
		return currentPos;
	}
}
