package insomnia.rule.tree;

import java.util.List;

public interface IPath extends ITree
{
	List<String> getKeys();

	boolean isTerminal();

	boolean isIncluded(IPath path);

	boolean isPrefix(IPath path);

	boolean isSuffix(IPath path);
	
	boolean hasPrefixInSuffix(IPath path);
}