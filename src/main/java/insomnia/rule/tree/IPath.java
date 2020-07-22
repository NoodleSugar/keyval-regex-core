package insomnia.rule.tree;

import java.util.List;

public interface IPath extends ITree
{
	List<String> getKeys();
	
	int getSize();

	boolean isTerminal();

	boolean isIncluded(IPath path);

	boolean isPrefix(IPath path);

	boolean isSuffix(IPath path);
	
	boolean isEqual(IPath path);
	
	boolean hasPrefixInSuffix(IPath path);
}