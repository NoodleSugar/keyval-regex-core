package insomnia.implem.kv.data;

import java.util.List;

import insomnia.data.INode;

public class KVNode implements INode<KVValue, KVLabel>
{
	KVValue value;

	KVNode parent;

	List<KVNode> childs;

	KVNode()
	{

	}

	KVNode(KVNode parent)
	{
		this.parent = parent;
	}

	@Override
	public KVValue getValue()
	{
		return value;
	}

	@Override
	public void setValue(KVValue value)
	{
		this.value = value;
	}

//	@Override
//	public List<KVEdge> getParents()
//	{
//		return null;
//	}
//
//	@Override
//	public List<KVEdge> getChildren()
//	{
//		return null;
//	}

}
