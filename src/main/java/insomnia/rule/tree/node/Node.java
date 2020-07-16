package insomnia.rule.tree.node;

public class Node implements INode
{
	@Override
	public boolean isRoot()
	{
		return false;
	}
	
	@Override
	public boolean isLeaf()
	{
		return false;
	}
}
