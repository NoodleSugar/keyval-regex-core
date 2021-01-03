package insomnia.implem.kv.data;

import java.util.List;
import java.util.Optional;

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
	public Optional<KVValue> getValue()
	{
		return Optional.ofNullable(value);
	}
}
