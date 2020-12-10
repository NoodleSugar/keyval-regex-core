package insomnia.implem.kv.data;

import insomnia.data.INode;

class KVPathNode implements INode<KVValue, KVLabel>
{
	int pos;

	KVPathNode(int pos)
	{
		this.pos = pos;
	}

	@Override
	public KVValue getValue()
	{
		return null;
	}

	@Override
	public void setValue(KVValue value)
	{
		throw new UnsupportedOperationException();
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
