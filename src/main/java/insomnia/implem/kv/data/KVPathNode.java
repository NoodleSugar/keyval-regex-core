package insomnia.implem.kv.data;

import java.util.Optional;

import insomnia.data.INode;

class KVPathNode implements INode<KVValue, KVLabel>
{
	int     pos;
	KVValue value;

	KVPathNode(int pos, KVValue value)
	{
		this.pos   = pos;
		this.value = value;
	}

	@Override
	public Optional<KVValue> getValue()
	{
		return Optional.ofNullable(value);
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
