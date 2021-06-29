package insomnia.implem.dataguide;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.IterableUtils;

import insomnia.data.IEdge;
import insomnia.data.INode;
import insomnia.data.IPath;
import insomnia.data.ITree;
import insomnia.data.creational.ITreeBuilder;
import insomnia.dataguide.IClassicDataGuide;
import insomnia.implem.data.Trees;
import insomnia.implem.data.creational.TreeBuilder;
import insomnia.implem.fsa.fta.buftachunk.modifier.IBUFTAChunkModifier;

/**
 * Dataguide defined from <a href="http://www.vldb.org/conf/1997/P436.PDF">DataGuides: Enabling Query Formulation and Optimization in Semistructured Databases</a>.
 * 
 * @author zuri
 */
public final class ClassicDataguide<VAL, LBL> implements IClassicDataGuide<VAL, LBL>
{
	private ITreeBuilder<EnumSet<NodeType>, LBL> tbuilder;

	public ClassicDataguide()
	{
		tbuilder = new TreeBuilder<>();
		tbuilder.setValue(EnumSet.of(NodeType.OBJECT));
	}

	// ==========================================================================

	private void addPath(ITree<VAL, LBL> parentTree, IPath<VAL, LBL> path)
	{
		boolean parentIsPath = null == parentTree;

		if (parentIsPath)
			parentTree = path;

		var bnode = tbuilder.getRoot();
		var pnode = path.getRoot();

		// Seach common prefix
		for (;;)
		{
			tbuilder.setCurrentNode(bnode);
			var pedge = path.getChild(pnode);

			if (null == pedge)
				return;

			var plabel = pedge.getLabel();
			var bedges = tbuilder.getEdges(bnode);
			var bedge  = IterableUtils.find(bedges, e -> Objects.equals(e.getLabel(), plabel));

			if (null == bedge)
				break;

			pnode = path.getChild(pnode).getChild();
			bnode = bedge.getChild();

			var types = typeOf(parentTree, pedge);

			for (var t : types)
				bnode.getValue().add(t);
		}

		// Add the remaining path
		{
			var subPath = createRemainingPath(parentTree, (IPath<VAL, LBL>) Trees.subTree(path, pnode));
			tbuilder.tree(subPath, false);
		}

		if (parentIsPath)
			parentTree = null;
	}

	private EnumSet<NodeType> typeOf(ITree<VAL, LBL> parentTree, IEdge<VAL, LBL> edge)
	{
		var  label       = edge.getLabel();
		long parentEdges = parentTree.getChildren(edge.getParent()).stream().filter(e -> Objects.equals(e.getLabel(), label)).count();

		if (parentEdges == 1)
			return EnumSet.of(NodeType.OBJECT);

		return EnumSet.of(NodeType.ARRAY);
	}

	private IPath<EnumSet<NodeType>, LBL> createRemainingPath(ITree<VAL, LBL> parentTree, IPath<VAL, LBL> rpath)
	{
		ITreeBuilder<EnumSet<NodeType>, LBL> tbuilder = new TreeBuilder<>();
		INode<VAL, LBL>                      rnode    = rpath.getRoot();

		for (;;)
		{
			var redge = rpath.getChild(rnode);

			if (null == redge)
				break;

			tbuilder.addChildDown(redge.getLabel(), typeOf(parentTree, redge));
			rnode = redge.getChild();
		}
		return (IPath<EnumSet<NodeType>, LBL>) Trees.create(tbuilder);
	}

	@Override
	public void addTree(ITree<VAL, LBL> tree)
	{
		for (var path : ITree.getPaths(tree))
			addPath(tree, path);
	}

	@Override
	public IBUFTAChunkModifier<VAL, LBL> getModifier()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITree<EnumSet<NodeType>, LBL> getTree()
	{
		return tbuilder;
	}

	public EnumSet<NodeType> getType(List<LBL> path)
	{
		return IClassicDataGuide.getType(this, path);
	}

	@Override
	public String toString()
	{
		return tbuilder.toString();
	}
}
