package insomnia.implem.kv.rule.grd;

import org.jgrapht.graph.DefaultDirectedGraph;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.rule.IRule;
import insomnia.rule.dependency.IDependency;
import insomnia.rule.grd.IGRD;

class KVGRD extends DefaultDirectedGraph<IRule<KVValue, KVLabel>, IDependency<KVValue, KVLabel>> implements IGRD<KVValue, KVLabel>
{
	private static final long serialVersionUID = 1L;

	public KVGRD()
	{
		super(null, null, false);
	}
}