package insomnia.implem.kv.data;

public class KVLabelFactory
{
	KVLabel get(String label)
	{
		return new KVLabel(label);
	}
}
