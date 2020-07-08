package insomnia.rule.tree;

import java.util.List;
import java.util.Map.Entry;

public interface ITree // implements List<IPath>
{
	INode getRoot();

	INode getFather(INode node) throws TreeException;

	List<Entry<String, INode>> getChildren(INode node) throws TreeException;
}