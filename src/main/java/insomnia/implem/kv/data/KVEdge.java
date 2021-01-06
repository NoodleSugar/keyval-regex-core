package insomnia.implem.kv.data;

import insomnia.data.IEdge;
import insomnia.data.INode;

public class KVEdge implements IEdge<KVValue, KVLabel>
{
	INode<KVValue,KVLabel> parent;
	INode<KVValue,KVLabel> child;

	KVLabel label;

	public KVEdge(INode<KVValue,KVLabel> parent, INode<KVValue,KVLabel> child, KVLabel label)
	{
		this.parent = parent;
		this.child  = child;
		this.label  = label;
	}

	@Override
	public KVLabel getLabel()
	{
		return label;
	}

	@Override
	public INode<KVValue,KVLabel> getParent()
	{
		return parent;
	}

	@Override
	public INode<KVValue,KVLabel> getChild()
	{
		return child;
	}

}
