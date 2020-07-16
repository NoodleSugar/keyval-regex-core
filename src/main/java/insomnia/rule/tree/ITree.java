package insomnia.rule.tree;

import java.util.List;
import java.util.Map.Entry;

import insomnia.rule.tree.node.INode;

public interface ITree
{
	INode getRoot();

	INode getParent(INode node) throws TreeException;

	List<Entry<String, INode>> getChildren(INode node) throws TreeException;
	
	boolean isRooted();
}